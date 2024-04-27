package models.services

import java.util.UUID

import play.silhouette.api.services.IdentityService
import play.silhouette.impl.providers.CommonSocialProfile
import models.User

import scala.concurrent.Future

/**
 * Handles actions related to users.
 *
 * This trait defines methods for retrieving, saving and updating user data. It also provides a method to retrieve an admin user.
 *
 * @tparam User The type of user object to be handled by this service.
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile): Future[User]

  
  /**
   * Retrieves the admin user asynchronously.
   *
   * @return A future containing an optional Admin User. The Future will be completed with Some(User) if an admin user
   *         exists, or None if no admin user is found.
   */
  def adminUser(): Future[Option[User]]
}
