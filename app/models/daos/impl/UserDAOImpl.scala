package models.daos.impl

import jakarta.inject.Inject
import models.User
import models.UserRoles
import models.daos.UserDAO
import play.api.db.slick.DatabaseConfigProvider
import play.silhouette.api.LoginInfo

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class UserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ex: ExecutionContext) extends UserDAO with AuthDAOSlick {

  import profile.api._

  override def find(userID: UUID): Future[Option[User]] =  {
    val query = for {
      dbUser <- slickUsers.filter(_.id === userID)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)
    db.run(query.result.headOption).map { resultOption =>
      resultOption.map { case (user, loginInfo) => mapUserFromDBModel(user, loginInfo) }
    }
  }

  override def find(loginInfo: LoginInfo, email: Option[String]): Future[Option[User]] = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(user => user.id === dbUserLoginInfo.userID)
    } yield dbUser

    val emailQuery = for {
      dbUser <- slickUsers.filter(_.email === email)
    } yield dbUser

    db.run((userQuery union emailQuery).result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        User(user.userID, loginInfo, user.firstName, user.lastName, user.email, user.avatarURL, user.activated, UserRoles.apply(user.role))
      }
    }
  }

  override def save(user: User): Future[User] = {
    val dbUser = DbUser(user.userID, user.firstName, user.lastName, user.email, user.avatarURL, user.activated, user.role.value)
    val dbLoginInfo = DbLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this user we retrieve the id on insertion.
    val loginInfoAction = {
      val retrieveLoginInfo = slickLoginInfos.filter(
        info => info.providerID === user.loginInfo.providerID &&
          info.providerKey === user.loginInfo.providerKey).result.headOption
      val insertLoginInfo = slickLoginInfos.returning(slickLoginInfos.map(_.id)).
        into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      for {
        loginInfoOption <- retrieveLoginInfo
        loginInfo <- loginInfoOption.map(DBIO.successful).getOrElse(insertLoginInfo)
      } yield loginInfo
    }
    // combine database actions to be run sequentially
    val actions = (for {
      _ <- slickUsers.insertOrUpdate(dbUser)
      loginInfo <- loginInfoAction
      _ <- slickUserLoginInfos.insertOrUpdate(DbUserLoginInfo(dbUser.userID, loginInfo.id.get))
    } yield ()).transactionally
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }

  override def adminUser(): Future[Option[User]] = {
    val query = for {
      dbUser <- slickUsers.filter(_.role === UserRoles.Admin.value)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.userID === dbUser.id)
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield (dbUser, dbLoginInfo)

    db.run(query.result.headOption).map { resultOption =>
      resultOption.map { case (user: DbUser, loginInfo: DbLoginInfo) => mapUserFromDBModel(user, loginInfo) }
    }
  }

  private def mapUserFromDBModel(user: DbUser, loginInfo: DbLoginInfo) = {
    User(
      user.userID,
      LoginInfo(loginInfo.providerID, loginInfo.providerKey),
      user.firstName,
      user.lastName,
      user.email,
      user.avatarURL,
      user.activated,
      UserRoles.apply(user.role)
    )
  }
}
