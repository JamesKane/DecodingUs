package controllers

import org.webjars.play.WebJarsUtil
import play.api.*
import play.api.mvc.*
import play.silhouette.api.LogoutEvent
import play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}
import utils.route.Calls

import jakarta.inject.{Inject, Singleton}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val scc: SilhouetteControllerComponents,
                               cookieView: views.html.fixed.cookieUse,
                               termsView: views.html.fixed.terms,
                               faqView: views.html.fixed.faq,
                               aboutView: views.html.fixed.about,
                               contactView: views.html.fixed.contact,
                               memberTierView: views.html.fixed.memberTier)
                              (implicit webjars: WebJarsUtil, assets: AssetsFinder) extends SilhouetteController(scc) {

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

  def terms = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(termsView(request.identity))
  }

  def cookieUse = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(cookieView(request.identity))
  }

  def faq = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(faqView(request.identity))
  }

  def about = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(aboutView(request.identity))
  }

  def contact = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(contactView(request.identity))
  }

  def memberTiers = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(memberTierView(request.identity))
  }
}
