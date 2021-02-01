package controllers

import dataaccess.UsersDAO
import models.User
import play.api.mvc.{Request, Result}

import scala.concurrent.{ExecutionContext, Future}

/**
  * A place to store methods that are useful to controllers.
  */
object ControllerUtils {
  
  /**
    * Perform a different action, depending on whether there's a user logged in or not.
    * @param withU
    * @param noU
    * @param req
    * @param ec
    * @param users
    * @return
    */
  def subjectPresentOrElse(users:UsersDAO, withU:(User, Request[_])=>Result, noU:Request[_]=>Result )(implicit req:Request[_], ec:ExecutionContext):Future[Result] = {
    req.session.get(security.DeadboltHandler.USER_ID_SESSION_KEY) match {
      case None => Future(noU(req))
      case Some(usrId) => users.get(usrId.toLong).map( {
        case None => noU(req).withNewSession
        case Some(u) => withU(u,req)
      })
    }
  }
  
  private val IDENTIFIER_CHARS=Set('-','.')
  
  /**
    * Cleans the input string such that it contains only
    * visible characters that are OK to use in an identifier.
    *
    * e.g. removes all kinds of unicode ghosts, such as U+FEFF.
    *
    * @param in suspicious string
    * @return an OK string to use as a textual record identifier
    */
  def cleanIdentifierString(in:String):String={
    import java.lang.Character._
    in.filter(c=>isAlphabetic(c)||isDigit(c)||IDENTIFIER_CHARS(c))
  }
}
