package models.services.impl

import play.silhouette.api.LoginInfo
import play.silhouette.impl.providers.CommonSocialProfile
import models.daos.UserDAO
import models.services.UserService
import models.{ User, UserRoles }

import java.time.ZonedDateTime
import java.util.UUID
import jakarta.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 * @param ex      The execution context.
 */
class UserServiceImpl @Inject() (userDAO: UserDAO)(implicit ex: ExecutionContext) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID) = userDAO.find(id)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo, None)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = userDAO.save(user)

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo, profile.email).flatMap {
      case Some(user) => // Update user with profile
        userDAO.save(user.copy(
          firstName = profile.firstName,
          lastName = profile.lastName,
          email = profile.email,
          avatarURL = profile.avatarURL
        ))
      case None => // Insert a new user
        userDAO.save(User(
          userID = UUID.randomUUID(),
          loginInfo = profile.loginInfo,
          firstName = profile.firstName,
          lastName = profile.lastName,
          email = profile.email,
          avatarURL = profile.avatarURL,
          activated = false,
          role = UserRoles.User
        ))
    }
  }

  override def adminUser(): Future[Option[User]] = userDAO.adminUser()
}
