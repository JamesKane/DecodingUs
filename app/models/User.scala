package models

import play.silhouette.api.{ Identity, LoginInfo }

import java.time.ZonedDateTime
import java.util.UUID

/**
 * The user object.
 *
 * @param userID    The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param firstName Maybe the first name of the authenticated user.
 * @param lastName  Maybe the last name of the authenticated user.
 * @param email     Maybe the email of the authenticated provider.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 * @param activated Indicates that the user has activated its registration.
 */
case class User(
                 userID: UUID,
                 loginInfo: LoginInfo,
                 firstName: Option[String],
                 lastName: Option[String],
                 email: Option[String],
                 avatarURL: Option[String],
                 activated: Boolean,
                 role: UserRoles
               ) extends Identity {

  /**
   * Tries to construct a name.
   *
   * @return Maybe a name.
   */
  def name =
    firstName -> lastName match {
      case (Some(f), Some(l)) => Some(f + " " + l)
      case (Some(f), None) => Some(f)
      case (None, Some(l)) => Some(l)
      case _ => None
    }

}
