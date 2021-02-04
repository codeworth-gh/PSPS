package controllers

import akka.serialization.JSerializer
import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}
import play.api.mvc.{ControllerHelpers, Result}

/**
 * Mix this helper with a controller class to get some useful JSON-related methods.
 */
trait JsonApiHelper extends ControllerHelpers {
  
  val okJson:Result = Ok(Json.obj("status"->"success"))
  def okJson(msg:String):Result = Ok(Json.obj("status"->"success", "message"->msg))
  
  /**
    * Translates a JsError (e.g. from parsing some JSON) to an informative JSON error response.
    * @param errList
    * @return A `Result` describing the errors in `errList`.
    */
  def badRequestJson( errList:scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])] ):Result = {
    BadRequest(Json.obj("status"->"error",
      "message"->errList.map( e => e._1.toString +": " + e._2.map(_.toString).mkString("[",", ","]")).mkString("\n")))
  }
  
  def badRequestJson(err:JsError):Result = badRequestJson(err.errors)
  
  def badRequestJson(err:String):Result = BadRequest(Json.obj("status"->"error", "message"->err))
  
  def notFoundJson( msg:String ):Result = NotFound(
    Json.obj("status"->"not found", "message"->msg)
  )
  
  
}
