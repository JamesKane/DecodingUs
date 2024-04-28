package controllers

import jakarta.inject.{Inject, Singleton}
import org.webjars.play.WebJarsUtil
import play.api.mvc.AnyContent
import play.silhouette.api.actions.UserAwareRequest

@Singleton
class AcademicController @Inject()(val scc: SilhouetteControllerComponents,
                                   paperView: views.html.paper)
                                  (implicit webjars: WebJarsUtil, assets: AssetsFinder) extends SilhouetteController(scc) {
  def papers = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(paperView(request.identity))
  }
}