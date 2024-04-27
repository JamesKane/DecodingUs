package controllers

import models.User
import play.api.mvc.RequestHeader
import play.silhouette.api.LoginEvent
import play.silhouette.api.services.AuthenticatorResult
import utils.route.Calls

import scala.concurrent.{ExecutionContext, Future}

/**
 * `AbstractAuthController` base with support methods to authenticate an user.
 *
 * @param scc The Silhouette stack.
 * @param ex  The execution context.
 */
abstract class AbstractAuthController(
                                       scc: SilhouetteControllerComponents
                                     )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  /**
   * Performs user authentication
   *
   * @param user       User data
   * @param rememberMe Remember me flag
   * @param request    Initial request
   * @return The result to display.
   */
  protected def authenticateUser(user: User, rememberMe: Boolean)(implicit request: RequestHeader): Future[AuthenticatorResult] = {
    import play.silhouette.api.Authenticator.Implicits.RichDateTime
    val result = Redirect(Calls.home)
    authenticatorService.create(user.loginInfo).map {
      case authenticator if rememberMe =>
        authenticator.copy(
          expirationDateTime = clock.now + scc.rememberMeConfig.expiry,
          idleTimeout = scc.rememberMeConfig.idleTimeout,
          cookieMaxAge = scc.rememberMeConfig.cookieMaxAge
        )
      case authenticator => authenticator
    }.flatMap { authenticator =>
      eventBus.publish(LoginEvent(user, request))
      authenticatorService.init(authenticator).flatMap { v =>
        authenticatorService.embed(v, result)
      }
    }
  }
}
