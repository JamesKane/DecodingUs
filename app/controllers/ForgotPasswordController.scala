package controllers

import play.silhouette.api._
import play.silhouette.impl.providers.CredentialsProvider
import forms.ForgotPasswordForm
import play.api.i18n.Messages
import play.api.libs.mailer.Email
import play.api.mvc.{AnyContent, Request}
import utils.route.Calls

import jakarta.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
 * The `Forgot Password` controller.
 */
class ForgotPasswordController @Inject()(
                                          components: SilhouetteControllerComponents,
                                          forgotPassword: views.html.forgotPassword,
                                        )(implicit ex: ExecutionContext) extends SilhouetteController(components) {

  /**
   * Views the `Forgot Password` page.
   *
   * @return The result to display.
   */
  def view = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(forgotPassword(ForgotPasswordForm.form)))
  }

  /**
   * Sends an email with password reset instructions.
   *
   * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
   * a notice for not existing email addresses to prevent the leak of existing email addresses.
   *
   * @return The result to display.
   */
  def submit = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    ForgotPasswordForm.form.bindFromRequest().fold(
      form => Future.successful(BadRequest(forgotPassword(form))),
      email => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        val result = Redirect(Calls.signin).flashing("info" -> Messages("reset.email.sent"))
        userService.retrieve(loginInfo).flatMap {
          case Some(user) if user.email.isDefined =>
            authTokenService.create(user.userID, 24 hours).map { authToken =>
              val url = routes.ResetPasswordController.view(authToken.id).absoluteURL()

              mailerClient.send(Email(
                subject = Messages("email.reset.password.subject"),
                from = Messages("email.from"),
                to = Seq(email),
                bodyText = Some(views.txt.emails.resetPassword(user, url).body),
                bodyHtml = Some(views.html.emails.resetPassword(user, url).body)
              ))
              result
            }
          case _ => Future.successful(result)
        }
      }
    )
  }
}
