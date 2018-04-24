package controllers

import java.sql.Timestamp
import java.util.UUID

import be.objectify.deadbolt.scala.DeadboltActions
import dataaccess.{InvitationDAO, UsersDAO, UuidForForgotPasswordDAO}
import javax.inject.Inject
import models.{Invitation, User, UuidForForgotPassword}
import play.api.{Configuration, Logger, cache}
import play.api.cache.Cached
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{JsObject, JsString}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{Action, ControllerComponents, InjectedController}
import security.UserSubject

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


case class UserFormData( username:String,
                         name:String,
                         email:Option[String],
                         pass1:Option[String],
                         pass2:Option[String],
                         uuid:Option[String]) {
  def update(u:User) = u.copy(name=name, email=email.getOrElse(""))
}
object UserFormData {
  def of( u:User ) = UserFormData(u.username, u.name, Option(u.email), Option(""), Option(""), None)
}
case class LoginFormData( username:String, password:String )
case class ForgotPassFormData ( email:String , protocol_and_host:String)
case class ResetPassFormData ( password1:String, password2:String, uuid:String)
case class ChangePassFormData ( previousPassword:String, password1:String, password2:String)

class UserCtrl @Inject()(deadbolt:DeadboltActions, conf:Configuration,
                         cached: Cached, cc:ControllerComponents,
                         users: UsersDAO, uuidForInvitation:InvitationDAO,
                         uuidForForgotPassword:UuidForForgotPasswordDAO,
                         mailerClient: MailerClient) extends InjectedController {

  implicit private val ec = cc.executionContext
  private val validUserId = "^[-._a-zA-Z0-9]+$".r

  val userForm = Form(mapping(
      "username" -> text(minLength = 1, maxLength = 64)
        .verifying( "Illegal characters found. Use letters, numbers, and -_. only.", s=>validUserId.findFirstIn(s).isDefined),
      "name"     -> nonEmptyText,
      "email"    -> optional(email),
      "password1" -> optional(text),
      "password2" -> optional(text),
      "uuid"      -> optional(text)
    )(UserFormData.apply)(UserFormData.unapply)
  )

  val loginForm = Form(mapping(
    "username" -> text,
    "password" -> text
  )(LoginFormData.apply)(LoginFormData.unapply)
  )

  val emailForm = Form(mapping(
    "email" -> text,
    "protocol_and_host" -> text
  )(ForgotPassFormData.apply)(ForgotPassFormData.unapply)
  )

  val resetPassForm = Form(mapping(
    "password1" -> text,
    "password2" -> text,
    "uuid" -> text
  )(ResetPassFormData.apply)(ResetPassFormData.unapply)
  )

  val changePassForm = Form(mapping(
    "previousPassword" -> text,
    "password1" -> text,
    "password2" -> text
  )(ChangePassFormData.apply)(ChangePassFormData.unapply)
  )

  def index = Action {
    Ok(views.html.index(None,None))
  }

  def doLogin = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      badForm   => Future(BadRequest(views.html.index(None, Some("Error processing login form")))),
      loginData => {
        users.authenticate(loginData.username.trim, loginData.password.trim)
          .map( _.map(u => Redirect(routes.UserCtrl.userHome).withNewSession.withSession(("userId",u.id.toString)))
            .getOrElse( {
              val form = loginForm.fill(loginData).withGlobalError("Bad username or password")
              BadRequest(views.html.users.login(Some(loginData.username),
                Some("Username/Password does not match")))})
          )
      }
    )
  }

  def userHome = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    Future(Ok( views.html.userHome(user) ))
  }

  def apiAddUser = Action(parse.tolerantJson).async { req =>
    if ( req.connection.remoteAddress.isLoopbackAddress ) {
      val payload = req.body.asInstanceOf[JsObject]
      val username = payload("username").as[JsString].value
      val password = payload("password").as[JsString].value
      val email = payload("email").as[JsString].value
      val user = User(0, username, "", email, users.hashPassword(password))

      users.addUser(user).map(u => Ok("Added user " + u.username))

    } else {
      Future( Forbidden("Adding users via API is only available from localhost") )
    }
  }


  def showEditUserPage( userId:String ) = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    if ( userId ==  user.username ) {
      users.get(userId).map({
        case None => notFound(userId)
        case Some(user) => Ok(
          views.html.users.userEditor(userForm.fill(UserFormData.of(user)),
            routes.UserCtrl.doSaveUser(user.username),
            isNew=false, false))
      })
    } else {
      Future( Forbidden("A user cannot edit the profile of another user.") )
    }
  }

  def doSaveUser(userId:String) = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    if ( userId == user.username ) {
      userForm.bindFromRequest().fold(
        fwe => Future(BadRequest(views.html.users.userEditor(fwe, routes.UserCtrl.doSaveUser(userId), isNew = false, false))),
        fData => {
          for {
            userOpt <- users.get(userId)
            _ <- userOpt.map( user => users.updateUser(fData.update(user)) ).getOrElse(Future(()))
          } yield {
            userOpt.map(_ => Redirect(routes.UserCtrl.showUserList))
              .getOrElse(notFound(userId))
          }
        }
      )
    } else {
      Future( Forbidden("A user cannot edit the profile of another user.") )
    }
  }

  def showNewUserPage = deadbolt.SubjectPresent()(){ implicit req =>
    Future(Ok( views.html.users.userEditor(userForm, routes.UserCtrl.doSaveNewUser, isNew=true, isInvite=false) ))
  }

  def doSaveNewUser = deadbolt.SubjectPresent()(){ implicit req =>
    userForm.bindFromRequest().fold(
      fwe => Future(BadRequest(views.html.users.userEditor(fwe, routes.UserCtrl.doSaveNewUser, isNew=true, isInvite=false))),
      fData => {
        val res = for {
          usernameExists <- users.usernameExists(fData.username)
          emailExists    <- fData.email.map(users.emailExists).getOrElse(Future(false))
          passwordOK     = fData.pass1.nonEmpty && fData.pass1 == fData.pass2
          canCreateUser  = !usernameExists && !emailExists && passwordOK

        } yield {
          if ( canCreateUser ) {
            val user = User(0, fData.username, fData.name, fData.email.getOrElse(""),
              users.hashPassword(fData.pass1.get))
            users.addUser(user).map( _ => Redirect(routes.UserCtrl.showUserList()) )

          } else {
            var form = userForm.fill(fData)
            if ( emailExists ) form = form.withError("email", "Email already exists")
            if ( usernameExists ) form = form.withError("username", "Username already taken")
            if ( !passwordOK ) form = form.withError("password1", "Passwords must match, and cannot be empty")
              .withError("password2", "Passwords must match, and cannot be empty")
            Future(BadRequest(views.html.users.userEditor(form, routes.UserCtrl.doSaveNewUser, isNew = true, isInvite=false)))
          }
        }

        scala.concurrent.Await.result(res, Duration(2000, scala.concurrent.duration.MILLISECONDS))

      }
    )
  }

  def showNewUserInvitation(uuid:String) = Action { req =>
    Ok( views.html.users.userEditor( userForm.bind(Map("uuid"->uuid)).discardingErrors, routes.UserCtrl.doNewUserInvitation,
      isNew=true, isInvite=true ))
  }

  def doNewUserInvitation() = Action.async { implicit req =>
    userForm.bindFromRequest().fold(
      fwe => {
        Future(BadRequest(views.html.users.userEditor(fwe, routes.UserCtrl.doNewUserInvitation, isNew=true, isInvite=true)))
      },
      fData => {
        val res = for {
          uuidExists     <- fData.uuid.map(uuidForInvitation.uuidExists).getOrElse(Future(false))
          usernameExists <- users.usernameExists(fData.username)
          emailExists    <- fData.email.map(users.emailExists).getOrElse(Future(false))
          passwordOK     = fData.pass1.nonEmpty && fData.pass1 == fData.pass2
          canCreateUser  = uuidExists && !usernameExists && !emailExists && passwordOK
        } yield {
          if (canCreateUser){
            val user = User(0, fData.username, fData.name, fData.email.getOrElse(""),
              users.hashPassword(fData.pass1.get))
            uuidForInvitation.deleteUuid(fData.uuid.get)
            users.addUser(user).map(_ => Redirect(routes.UserCtrl.index()))
          }
          else{
            var form = userForm.fill(fData)
            if ( !uuidExists ) form = form.withError("uuid", "invitation id does not exist")
            if ( usernameExists ) form = form.withError("username", "Username already taken")
            if ( emailExists ) form = form.withError("email", "Email already exists")
            if ( !passwordOK ) form = form.withError("password1", "Passwords must match, and cannot be empty")
              .withError("password2", "Passwords must match, and cannot be empty")
            Future(BadRequest(views.html.users.userEditor(form, routes.UserCtrl.doNewUserInvitation, isNew = true, isInvite=true)))
          }
        }
        scala.concurrent.Await.result(res, Duration(2000, scala.concurrent.duration.MILLISECONDS))

      }
    )

  }

  def showUserList = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    users.allUsers.map( users => Ok(views.html.users.userList(users, user)) )
  }

  def showLogin = Action { req =>
    Ok( views.html.users.login(None,None) )
  }

//  def doLogin = Action.async{ implicit req =>
//
////    loginForm.bindFromRequest().fold(
////      badForm   => Future(BadRequest(views.html.index(badForm.withGlobalError("Bad Request")))),
////      loginData => {
////        users.authenticate(loginData.username.trim, loginData.password.trim)
////          .map( _.map(u => Redirect(routes.HomeController.userHome).withNewSession.withSession(("userId",u.id.toString)))
////            .getOrElse( {
////              val form = loginForm.fill(loginData).withGlobalError("Bad username or password")
////              BadRequest(views.html.index(form))})
////          )
////      }
////    )
//    loginForm.bindFromRequest().fold(
//      fwi => Future(BadRequest(views.html.users.login(None,Some("Error processing login form")))),
//      fd => {
//        for {
//          userOpt <- users.get(fd.username)
//          passwordOK = userOpt.exists(users.verifyPassword(_, fd.password))
//
//        } yield {
//          if ( passwordOK ){
//            val userSessionId = UUID.randomUUID.toString
//            userOpt.map(u => {
//              Redirect(routes.UserCtrl.userHome).withNewSession.withSession( ("userId",u.id.toString))
//            }).getOrElse(BadRequest(views.html.users.login(Some(fd.username),
//              Some("Username/Password does not match"))))
//          } else {
//            BadRequest(views.html.users.login(Some(fd.username),
//              Some("Username/Password does not match")))
//          }
//        }
//      }
//    )
//  }

  def doLogout = Action { req =>
    Redirect(routes.UserCtrl.index()).withNewSession.flashing(("message","You have been logged out."))
  }

  private def notFound(userId:String) = NotFound("User with username '%s' does not exist.".format(userId))

  def doForgotPassword = Action.async{ implicit req =>
    emailForm.bindFromRequest().fold(
      fwi => Future(BadRequest(views.html.users.forgotPassword(None,Some("Error processing forgot password form")))),
      fd => {
        for {
          userOpt <- users.getUserByEmail(fd.email)
          userSessionId = UUID.randomUUID.toString
          emailExists <- userOpt.map(u => uuidForForgotPassword.addUuidForForgotPassword(
            UuidForForgotPassword(u.username, userSessionId, new Timestamp(System.currentTimeMillis())))
            .map(_=>true))
            .getOrElse(Future(false))
        } yield {
          if ( emailExists ){
            val bodyText = "To reset your password, please click the link below: \n " + fd.protocol_and_host + "/admin/resetPassword/" + userSessionId
            val email = Email("Forgot my password", conf.get[String]("play.mailer.user"), Seq(fd.email), bodyText = Some(bodyText))
            mailerClient.send(email)
            Redirect( routes.UserCtrl.index() )
          }
          else {
            BadRequest(views.html.users.forgotPassword(Some(fd.email), Some("email does not exist")))
          }
        }
      }
    )
  }

  def showForgotPassword = Action { req =>
    Ok( views.html.users.forgotPassword(None,None) )
  }

  def showResetPassword(randomUuid:String) = Action { req =>
    Ok( views.html.users.reset(None) )
  }

  def doResetPassword() = Action.async{ implicit req =>
    resetPassForm.bindFromRequest().fold(
      fwi => Future(BadRequest(views.html.users.reset(Some("Error processing reset password form")))),
      fd => {
        for {
          uuidOpt     <- uuidForForgotPassword.getUuidmeByUuid(fd.uuid)
          userOpt     <- uuidOpt.map(u => users.get(u.username)).getOrElse(Future(None))
          timeOK      =  uuidOpt.exists(u => {
            val oneWeek = 1000 * 60 * 60 * 24 * 7
            val currentTime = System.currentTimeMillis()
            currentTime - u.resetPasswordDate.getTime < oneWeek})
          passwordOK  =  fd.password1.nonEmpty && fd.password1 == fd.password2
          resetOK = passwordOK && timeOK
        } yield {
          if (resetOK) {
            userOpt.map(u => {
              uuidForForgotPassword.deleteUuid(u.username)
              users.updatePassword(u, fd.password1)
              Redirect(routes.UserCtrl.index())}
            ).getOrElse(BadRequest(views.html.users.reset(Some("uuid does not exist"))))
          } else {
            if ( !timeOK ){
              BadRequest(views.html.users.reset(Some("It's been more then a week")))
            } else {
              BadRequest(views.html.users.reset(Some("Passwords must match, and cannot be empty")))
            }
          }
        }
      }
    )
  }

  def showInviteUser = Action {req =>
    Ok( views.html.users.inviteUser() )
  }

  def doInviteUser = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    emailForm.bindFromRequest().fold(
      fwi => {
        Logger.info( fwi.errors.mkString("\n") )
        Future(BadRequest(views.html.users.inviteUser()))
      },
      fd => {
        val invitationId = UUID.randomUUID.toString
        uuidForInvitation.addUuid(Invitation(user.email, new Timestamp(System.currentTimeMillis()), invitationId, fd.email))
        val link = fd.protocol_and_host + "/admin/newUserInvitation/" + invitationId
        val bodyText = "You have been invited to join a policy models server, please click the link below \n" + link
        val email = Email("Invite user", conf.get[String]("play.mailer.user"), Seq(fd.email), Some(bodyText))
        mailerClient.send(email)
        Future(Redirect(routes.UserCtrl.userHome()))
      }
    )
  }

  def doChangePassword = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    changePassForm.bindFromRequest().fold(
      fwi => {
        Future(BadRequest(views.html.users.userEditor(userForm, routes.UserCtrl.doSaveNewUser, isNew = false, false)))
      },
      fd => {
        if(users.verifyPassword(user, fd.previousPassword)){
          if (fd.password1.nonEmpty && fd.password1 == fd.password2) {
            users.updatePassword(user, fd.password1).map(_ => Redirect(routes.UserCtrl.userHome()))
          } else {
            val form = userForm.fill(UserFormData of user).withError("password1", "Passwords must match, and cannot be empty")
              .withError("password2", "Passwords must match, and cannot be empty")
            Future(BadRequest(views.html.users.userEditor(form, routes.UserCtrl.doSaveNewUser, isNew = false, false, activeFirst=false)))
          }
        } else{
          val form = userForm.fill(UserFormData of user).withError("previousPassword", "incorrect password")
          Future(BadRequest( views.html.users.userEditor(form, routes.UserCtrl.doSaveNewUser, isNew=false, false, activeFirst=false )))
        }
      }
    )
  }
}
