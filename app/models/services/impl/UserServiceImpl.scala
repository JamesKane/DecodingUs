package models.services.impl

import models.User
import models.services.UserService
import play.silhouette.api.LoginInfo
import play.silhouette.impl.providers.CommonSocialProfile

import java.util.UUID
import scala.concurrent.Future

class UserServiceImpl extends UserService {
  override def retrieve(id: UUID): Future[Option[User]] = ???
  
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = ???

  override def save(user: User): Future[User] = ???

  override def save(profile: CommonSocialProfile): Future[User] = ???
}
