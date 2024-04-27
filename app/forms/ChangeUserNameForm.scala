package forms

import play.api.data.Forms._
import play.api.data._

object ChangeUserNameForm {
  val form = Form(tuple(
    "first-name" -> optional(nonEmptyText),
    "last-name" -> optional(nonEmptyText)
  ))
}
