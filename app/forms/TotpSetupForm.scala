package forms

import play.silhouette.api.util.PasswordInfo
import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the submission of the form with data for TOTP-authentication enabling
 */
object TotpSetupForm {
  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "sharedKey" -> nonEmptyText,
      "scratchCodes" -> seq(
        mapping(
          "hasher" -> nonEmptyText,
          "password" -> nonEmptyText,
          "salt" -> optional(nonEmptyText)
        )(PasswordInfo.apply)(Data.unapply)
      ),
      "scratchCodesPlain" -> seq(nonEmptyText),
      "verificationCode" -> nonEmptyText(minLength = 6, maxLength = 6)
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   * @param sharedKey Shared user key for TOTP authentication.
   * @param scratchCodes Scratch or recovery codes used for one time TOTP authentication.
   * @param verificationCode Verification code for TOTP-authentication
   */
  case class Data(
                   sharedKey: String,
                   scratchCodes: Seq[PasswordInfo],
                   scratchCodesPlain: Seq[String],
                   verificationCode: String = "")
  
  object Data {
    def unapply(passwordInfo: PasswordInfo): Option[(String, String, Option[String])] = Some((passwordInfo.hasher, passwordInfo.password, passwordInfo.salt))
    def unapply(data: Data): Option[(String, Seq[PasswordInfo], Seq[String], String)] = Some((data.sharedKey, data.scratchCodes, data.scratchCodesPlain, data.verificationCode))
  }
}
