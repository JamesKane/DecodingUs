package models.daos.impl

import models.User
import models.daos.UserDAO
import play.silhouette.api.LoginInfo

import java.util.UUID
import scala.concurrent.Future

class UserDAOImpl extends UserDAO {

  override def adminUser(): Future[Option[User]] = ???

  override def find(userID: UUID): Future[Option[User]] = ???

  override def find(loginInfo: LoginInfo, email: Option[String]): Future[Option[User]] = ???

  override def save(user: User): Future[User] = ???
  
}
