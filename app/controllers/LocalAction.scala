package controllers

import javax.inject.Inject
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class LocalAction @Inject()(cc:ControllerComponents) extends ActionBuilder[Request, AnyContent]{

  private implicit val ec = cc.executionContext

  def invokeBlock[A](request: Request[A], block: (Request[A])  => Future[Result]) = {
    if ( request.connection.remoteAddress.isLoopbackAddress ) {
      block(request)
    } else {
      Future.successful( Unauthorized("this action can be done only from localhost") )
    }
  }

  override protected def executionContext: ExecutionContext = cc.executionContext
  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

}
