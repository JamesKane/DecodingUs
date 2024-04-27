package controllers

import forms.SignUpForm
import play.silhouette.api._
import play.silhouette.impl.providers._
import models.{AuthToken, User, UserRoles}
import play.api.i18n.Messages
import play.api.libs.mailer.Email
import play.api.mvc.{AnyContent, Request, Result}
import utils.route.Calls

import java.util.UUID
import jakarta.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
 * The `Sign Up` controller.
 */
class SignUpController @Inject()(
                                  components: SilhouetteControllerComponents,
                                  signUp: views.html.auth.signUp,
                                )(implicit ex: ExecutionContext, assets: AssetsFinder) extends SilhouetteController(components) {

  /**
   * Views the `Sign Up` page.
   *
   * @return The result to display.
   */
  def view = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(signUp(SignUpForm.form)))
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest().fold(
      form => Future.successful(BadRequest(signUp(form))),
      data => processSubmission(data, resultForSubmission(data))
    )
  }

  // helper function to process submission
  private def processSubmission(data: SignUpForm.Data, result: Result)(implicit request: Request[AnyContent]) = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
    userService.retrieve(loginInfo).flatMap {
      case Some(user) => sendEmailToExistingUser(user, data.email, result)
      case None => createNewUser(data, loginInfo, result)
    }
  }

  // helper function to create new user
  private def createNewUser(data: SignUpForm.Data, loginInfo: LoginInfo, result: Result)(implicit request: Request[AnyContent]) = {
    val user = newUserFromFormData(data, loginInfo)
    val passwordInfo = passwordHasherRegistry.current.hash(data.password)
    for {
      avatar <- avatarService.retrieveURL(data.email)
      user <- userService.save(user.copy(avatarURL = avatar))
      authInfo <- authInfoRepository.add(loginInfo, passwordInfo)
      authToken <- authTokenService.create(user.userID)
    } yield sendEmailToNewUser(user, data.email, authToken, result)
  }

  // helper function to prepare result for submission
  private def resultForSubmission(data: SignUpForm.Data)(implicit messages: Messages): Result = {
    Redirect(routes.SignUpController.view).flashing("info" -> Messages("sign.up.email.sent", data.email))
  }

  // helper function to prepare new user from form data
  private def newUserFromFormData(data: SignUpForm.Data, loginInfo: LoginInfo) = {
    User(
      userID = UUID.randomUUID(),
      loginInfo = loginInfo,
      firstName = Some(data.firstName),
      lastName = Some(data.lastName),
      email = Some(data.email),
      avatarURL = None,
      activated = false,
      role = UserRoles.User
    )
  }

  private def sendEmailToExistingUser(user: User, email: String, result: Result)(implicit request: Request[AnyContent]): Future[Result] = {
    val url = Calls.signin.absoluteURL()
    mailerClient.send(Email(
      subject = Messages("email.already.signed.up.subject"),
      from = Messages("email.from"),
      to = Seq(email),
      bodyText = Some(views.txt.emails.alreadySignedUp(user, url).body),
      bodyHtml = Some(views.html.emails.alreadySignedUp(user, url).body)
    ))
    Future.successful(result)
  }

  private def sendEmailToNewUser(user: User, email: String, authToken: AuthToken, result: Result)(implicit request: Request[AnyContent]): Result = {
    val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
    mailerClient.send(Email(
      subject = Messages("email.sign.up.subject"),
      from = Messages("email.from"),
      to = Seq(email),
      bodyText = Some(views.txt.emails.signUp(user, url).body),
      bodyHtml = Some(views.html.emails.signUp(user, url).body)
    ))
    eventBus.publish(SignUpEvent(user, request))
    result
  }
}
