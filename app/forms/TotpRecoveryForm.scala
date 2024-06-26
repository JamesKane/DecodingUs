package forms

import play.api.data.Form
import play.api.data.Forms._

import java.util.UUID

/**
 * The form which handles the submission of the credentials plus verification code for TOTP-authentication
 */
object TotpRecoveryForm {
  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "userID" -> uuid,
      "sharedKey" -> nonEmptyText,
      "rememberMe" -> boolean,
      "recoveryCode" -> nonEmptyText(minLength = 8, maxLength = 8)
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   * @param userID The unique identifier of the user.
   * @param sharedKey the TOTP shared key
   * @param rememberMe Indicates if the user should stay logged in on the next visit.
   * @param recoveryCode Verification code for TOTP-authentication
   */
  case class Data(
                   userID: UUID,
                   sharedKey: String,
                   rememberMe: Boolean,
                   recoveryCode: String = "")
  
  object Data {
    def unapply(data: Data): Option[(UUID, String, Boolean, String)] = Some((data.userID, data.sharedKey, data.rememberMe, data.recoveryCode))
  }
}
