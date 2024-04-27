package controllers

import forms.ChangeUserNameForm
import jakarta.inject.Inject
import models.services.UserService
import play.api.data.Form
import play.silhouette.impl.providers.GoogleTotpInfo

import scala.concurrent.ExecutionContext

class UserController @Inject() (userService: UserService, scc: SilhouetteControllerComponents, home: views.html.home)
                               (implicit ex: ExecutionContext)
  extends SilhouetteController(scc) {

  def index = SecuredAction.async { implicit request =>
    authInfoRepository.find[GoogleTotpInfo](request.identity.loginInfo).map {
      totpInfoOpt =>
        val userForm: Form[(Option[String], Option[String])] =
          ChangeUserNameForm.form.fill((request.identity.firstName, request.identity.lastName))
        Ok(home(request.identity, userForm, totpInfoOpt))
    }
  }

  def update = SecuredAction.async { implicit request =>
    ChangeUserNameForm.form
      .bindFromRequest()
      .fold(
        errorForm => {
          authInfoRepository
            .find[GoogleTotpInfo](request.identity.loginInfo)
            .map { totpInfoOpt => BadRequest(home(request.identity, errorForm, totpInfoOpt)) }
        },
        user => {
          userService.save(request.identity.copy(firstName = user._1, lastName = user._2))
            .flatMap(u =>
                authInfoRepository
                  .find[GoogleTotpInfo](request.identity.loginInfo)
                  .map { totpInfoOpt =>
                    Ok(home(u, ChangeUserNameForm.form.fill((u.firstName, u.lastName)), totpInfoOpt))
                  }
            )
        }
      )
  }
}
