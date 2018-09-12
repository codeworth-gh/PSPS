import javax.activation.MimeType
import javax.inject._
import play.api.http.{DefaultHttpErrorHandler, MimeTypes}
import play.api._
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router

import scala.concurrent._

@Singleton
class ErrorHandler @Inject() (
                               env: Environment,
                               config: Configuration,
                               sourceMapper: OptionalSourceMapper,
                               router: Provider[Router],
                               messagesApi:MessagesApi,
                               ec: ExecutionContext
                             ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {
  
  private implicit val execCtxt = ec
  val accJson = play.api.mvc.Accepting(MimeTypes.JSON)
  val accHtml = play.api.mvc.Accepting(MimeTypes.HTML)
  
  private def logException( exception:UsefulException ) = {
    val incidentId = java.util.UUID.randomUUID.toString
    Logger.error( "[" + incidentId + "] play-id:" + exception.id)
    Logger.error( "[" + incidentId + "] title:" + exception.title)
    Logger.error( "[" + incidentId + "] description:" + exception.description)
    Logger.error( "[" + incidentId + "] cause:", exception.cause)
    incidentId
  }
  
  
  override protected def onDevServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    val incidentId  = logException(exception)
    
    request match {
      case accJson() => super.onDevServerError(request, exception)
      case accHtml() => Future(InternalServerError( Json.obj(
        "message"  -> "Internal Server Error",
        "title"  -> exception.title,
        "playId" -> exception.id,
        "description" -> exception.description,
        "incidentId"  -> incidentId)
      ))
      case _ => Future(InternalServerError("Internal Server Error: " + incidentId ))
    }
  }
  
  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    val incidentId = logException(exception)
    
    Future.successful(
      request match {
        case accHtml() => InternalServerError( views.html.errorPage(500,
                                                      "Oops - an error occurred",
                                                      Some("This is an internal server error. We will look at why it happened."),
                                                      Some(incidentId), request, makeMessages(request)))
        case accJson() => InternalServerError( Json.obj("message"->"Internal Server Error. See logs for details", "incidentId"->incidentId))
        case _ => InternalServerError("Internal Server Error: " + incidentId)
      }
    )
  }
  
  override def onForbidden(request: RequestHeader, message: String) = {
    Future.successful(
      Forbidden(
        views.html.errorPage(403,
          "Forbidden",
          Some("You have tried to access a resource you are not allowed to access. Sorry!"),
          None, request, makeMessages(request))
      )
    )
  }
  
  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    val effMessage = if(message.nonEmpty) message else { if(statusCode==404){"Page not Found"} else {"Error " + statusCode}}
    Future.successful(
      request match {
        case accHtml() => Status(statusCode)(views.html.errorPage(statusCode,
            effMessage,
            Some("The item you are looking for might have moved, or you may have accessed an invalid address."),
            None, request, makeMessages(request)))
        case accJson() => Status(statusCode)(Json.obj("message"->effMessage, "status"->statusCode))
        case _ => Status(statusCode)("Client error: " + message + " (" + statusCode + ")")
      }
    )
  }
  
  private def makeMessages( req:RequestHeader ) = messagesApi.preferred(req)
}