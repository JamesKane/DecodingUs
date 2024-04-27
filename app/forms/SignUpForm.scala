package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the sign up process.
 */
object SignUpForm {

  // Spammers may creatively construct an email with a dead link as part of the contact information.
  def validate(firstName: String, lastName: String): Option[(String, String)] = {
    val combined = firstName.appendedAll(lastName).toLowerCase()
    if (combined.contains("http://") || combined.contains("https://"))
      None
    else Some((firstName, lastName))
  }

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply).verifying(
      "Failed!",
      fields => fields match {
        case userData => validate(userData.firstName, userData.lastName).isDefined
      }
    )
  )

  /**
   * The form data.
   *
   * @param firstName The first name of a user.
   * @param lastName The last name of a user.
   * @param email The email of the user.
   * @param password The password of the user.
   */
  case class Data(
                   firstName: String,
                   lastName: String,
                   email: String,
                   password: String)
}
