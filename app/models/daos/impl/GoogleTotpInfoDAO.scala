package models.daos.impl

import jakarta.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.silhouette.api.LoginInfo
import play.silhouette.impl.providers
import play.silhouette.impl.providers.GoogleTotpInfo
import play.silhouette.persistence.daos.DelegableAuthInfoDAO

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class GoogleTotpInfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ex: ExecutionContext, val classTag: ClassTag[GoogleTotpInfo])
  extends DelegableAuthInfoDAO[GoogleTotpInfo] with AuthDAOSlick {

  import profile.api._

  override def find(loginInfo: LoginInfo): Future[Option[providers.GoogleTotpInfo]] = ???

  override def add(loginInfo: LoginInfo, authInfo: providers.GoogleTotpInfo): Future[providers.GoogleTotpInfo] = ???

  override def update(loginInfo: LoginInfo, authInfo: providers.GoogleTotpInfo): Future[providers.GoogleTotpInfo] = ???

  override def save(loginInfo: LoginInfo, authInfo: providers.GoogleTotpInfo): Future[providers.GoogleTotpInfo] = ???

  override def remove(loginInfo: LoginInfo): Future[Unit] = ???
}
