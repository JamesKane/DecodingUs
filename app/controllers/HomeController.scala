package controllers

import org.webjars.play.WebJarsUtil

import javax.inject.*
import play.api.*
import play.api.mvc.*
import play.silhouette.api.LogoutEvent
import play.silhouette.api.actions.SecuredRequest
import utils.route.Calls

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val scc: SilhouetteControllerComponents)(implicit webjars: WebJarsUtil, assets: AssetsFinder) extends SilhouetteController(scc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = SecuredAction.async { implicit request: SecuredRequest[EnvType, AnyContent] =>
    val result = Redirect(Calls.home)
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }
}
