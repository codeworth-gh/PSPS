package views


import play.api.data.{Field, FormError}
import play.api.mvc.Request

object Helpers {
  val msg2eng = Map(
    "error.email" -> "Invalid email address",
    "error.minLength" -> "Field cannot be empty"
  )

  def messageToEng( fe:FormError ):String = msg2eng.getOrElse(fe.message,fe.message)


  def fieldStatus(f:Field):String = if(f.hasErrors) "has-error" else ""
}