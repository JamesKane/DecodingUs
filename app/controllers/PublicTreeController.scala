package controllers

import jakarta.inject.{Inject, Singleton}
import org.webjars.play.WebJarsUtil
import play.api.mvc.AnyContent
import play.silhouette.api.actions.UserAwareRequest

@Singleton
class PublicTreeController @Inject()(val scc: SilhouetteControllerComponents,
                                     treeView: views.html.tree)
                                    (implicit webjars: WebJarsUtil, assets: AssetsFinder) extends SilhouetteController(scc) {
  def yTree = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(treeView(request.identity, Option(("Y-DNA Tree", s"https://decoding-us.com${controllers.routes.PublicTreeController.yTree.url}"))))
  }

  def mtTree = silhouette.UserAwareAction { implicit request: UserAwareRequest[EnvType, AnyContent] =>
    Ok(treeView(request.identity, Option(("Mitochondrial Tree", s"https://decoding-us.com${controllers.routes.PublicTreeController.mtTree.url}"))))
  }
}