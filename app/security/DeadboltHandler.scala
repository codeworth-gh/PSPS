package security

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.{Role, Subject}
import controllers.routes
import dataaccess.UsersDAO
import models.User
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


case class UserSubject(user:User) extends Subject
{
  override def identifier: String = user.username
  override def roles = List[Role]()
  override def permissions = Nil
}


/**
  * Created by michael on 12/3/17.
  */
class DeadboltHandler(users:UsersDAO) extends be.objectify.deadbolt.scala.DeadboltHandler {

  override def beforeAuthCheck[A](request: Request[A]) = Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]) = Future(None)

  override def getSubject[A](request: AuthenticatedRequest[A]):Future[Option[Subject]] = {
    request.session.get("userId").map( sId => users.get(sId.toLong).map(_.map(u=>UserSubject(u))) )
      .getOrElse( Future(None) )
  }

  /**
    * When authentication fails, store the target URL in the session and go to login page.
    * @param request the unauthorized request
    * @tparam A type of A
    * @return redirect to login page response
    */
  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = {
    Future {
      Results.Redirect(routes.UserCtrl.showLogin()).withSession(
        request.session + ("targetUrl" -> request.path)).flashing(("message","Please login before accessing this page"))
    }
  }


}