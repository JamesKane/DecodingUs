package controllers

import forms.{SignInForm, TotpForm}
import jakarta.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Request}
import play.silhouette.api.exceptions.ProviderException
import play.silhouette.api.util.Credentials
import play.silhouette.impl.exceptions.IdentityNotFoundException
import play.silhouette.impl.providers.GoogleTotpInfo
import utils.route.Calls

import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign In` controller.
 */
class SignInController @Inject() (
                                   scc: SilhouetteControllerComponents,
                                   signIn: views.html.auth.signIn,
                                   activateAccount: views.html.auth.activateAccount,
                                   totp: views.html.auth.totp
                                 )(implicit ex: ExecutionContext, assets: AssetsFinder) extends AbstractAuthController(scc) {

  /**
   * Views the `Sign In` page.
   *
   * @return The result to display.
   */
  def view = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(signIn(SignInForm.form, socialProviderRegistry)))
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest().fold(
      form => Future.successful(BadRequest(signIn(form, socialProviderRegistry))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) if !user.activated =>
              Future.successful(Ok(activateAccount(data.email)))
            case Some(user) =>
              authInfoRepository.find[GoogleTotpInfo](user.loginInfo).flatMap {
                case Some(totpInfo) => Future.successful(Ok(totp(TotpForm.form.fill(TotpForm.Data(
                  user.userID, totpInfo.sharedKey, data.rememberMe)))))
                case _ => authenticateUser(user, data.rememberMe)
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            Redirect(Calls.signin).flashing("error" -> Messages("invalid.credentials"))
        }
      }
    )
  }
}
