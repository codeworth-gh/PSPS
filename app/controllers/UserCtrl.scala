package controllers

import java.sql.Timestamp
import java.util.UUID

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import dataaccess.{InvitationDAO, PasswordResetRequestDAO, UsersDAO}
import javax.inject.Inject
import models.{Invitation, PasswordResetRequest, User}
import play.api.{Configuration, Logger, cache}
import play.api.cache.Cached
import play.api.data._
import play.api.data.Forms._
import play.api.i18n._
import play.api.libs.json.{JsObject, JsString}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{Action, Call, ControllerComponents, InjectedController, Result}
import security.UserSubject

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}


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
case class ForgotPassFormData ( email:String )
case class ResetPassFormData ( password1:String, password2:String, uuid:String)
case class ChangePassFormData ( previousPassword:String, password1:String, password2:String)

/**
  * Contoller for user-related actions (login, account mgmt...)
  * @param deadbolt
  * @param conf
  * @param cached
  * @param cc
  * @param users
  * @param invitations
  * @param forgotPasswords
  * @param mailerClient
  */
class UserCtrl @Inject()(deadbolt:DeadboltActions, conf:Configuration,
                         cached: Cached, cc:ControllerComponents,
                         users: UsersDAO, invitations:InvitationDAO,
                         forgotPasswords:PasswordResetRequestDAO,
                         mailerClient: MailerClient, langs:Langs,
                         messagesApi:MessagesApi, localAction:LocalAction) extends InjectedController {

  implicit private val ec: ExecutionContext = cc.executionContext
  private val logger = Logger(classOf[UserCtrl])

  private val validUserId = "^[-._a-zA-Z0-9]+$".r
  implicit val messagesProvider: MessagesProvider = {
    MessagesImpl(langs.availables.head, messagesApi)
  }
  
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
    "email" -> text
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
  
  
  def showLogin = Action { implicit req =>
    Ok( views.html.users.login(loginForm) )
  }
  
  def doLogin = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      badForm   => Future(BadRequest(views.html.users.login(badForm))),
      loginData => {
        users.authenticate(loginData.username.trim, loginData.password.trim)
          .map( _.map(u => Redirect(routes.UserCtrl.userHome).withNewSession.withSession(("userId",u.id.toString)))
            .getOrElse( {
              BadRequest(views.html.users.login(loginForm.fill(loginData).withGlobalError("login.error.badUsernameEmailOrPassword")))
            })
          )
      }
    )
  }

  def doLogout = Action { implicit req =>
    Redirect(routes.HomeCtrl.index()).withNewSession.flashing(FlashKeys.MESSAGE->Informational(Informational.Level.Success, Messages("login.logoutMessage"), "").encoded)
  }

  def userHome = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    
    Future(Ok( views.html.users.userHome(user) ))
  }

  def apiAddUser = localAction(parse.tolerantJson).async { request =>
      val payload = request.body.asInstanceOf[JsObject]
      val username = payload("username").as[JsString].value
      val password = payload("password").as[JsString].value
      val email = payload("email").as[JsString].value
      val user = User(0, username, "", email, users.hashPassword(password))

      users.addUser(user).map(u => Ok("Added user " + u.username))
  }


  def showEditUserPage( userId:String ) = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    if ( userId ==  user.username ) {
      users.get(userId).map({
        case None => notFound(userId)
        case Some(user) => Ok(
          views.html.users.userEditor(userForm.fill(UserFormData.of(user)),
            routes.UserCtrl.doSaveUser(user.username),
            isNew=false))
      })
    } else {
      Future( Forbidden("A user cannot edit the profile of another user.") )
    }
  }

  def doSaveUser(userId:String) = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    if ( userId == user.username ) {
      userForm.bindFromRequest().fold(
        fwe => Future(BadRequest(views.html.users.userEditor(fwe, routes.UserCtrl.doSaveUser(userId), isNew = false))),
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
    Future(Ok( views.html.users.userEditor(userForm, routes.UserCtrl.doSaveNewUser, isNew=true) ))
  }

  def doSaveNewUser = deadbolt.SubjectPresent()(){ implicit req =>
    userForm.bindFromRequest().fold(
      fwe => Future(BadRequest(views.html.users.userEditor(fwe, routes.UserCtrl.doSaveNewUser, isNew=true))),
      fData => processUserForm( fData, routes.UserCtrl.showLogin, routes.UserCtrl.doSignup, true)(new AuthenticatedRequest(req, None))
    )
  }

  def showNewUserInvitation(uuid:String) = Action { implicit req =>
    Ok( views.html.users.userEditor( userForm.bind(Map("uuid"->uuid)).discardingErrors, routes.UserCtrl.doNewUserInvitation,
      isNew=true)(new AuthenticatedRequest(req, None), messagesProvider))
  }

  def doNewUserInvitation() = Action.async { implicit req =>
    userForm.bindFromRequest().fold(
      fwe => {
        Future(BadRequest(views.html.users.userEditor(fwe, routes.UserCtrl.doNewUserInvitation, isNew=true
                  )(new AuthenticatedRequest(req, None), messagesProvider)))
      },
      fData => {
        val res = for {
          uuidExists     <- fData.uuid.map(invitations.exists).getOrElse(Future(false))
          usernameExists <- users.usernameExists(fData.username)
          emailExists    <- fData.email.map(users.emailExists).getOrElse(Future(false))
          passwordOK     = fData.pass1.nonEmpty && fData.pass1 == fData.pass2
          canCreateUser  = uuidExists && !usernameExists && !emailExists && passwordOK
        } yield {
          if (canCreateUser){
            val user = User(0, fData.username, fData.name, fData.email.getOrElse(""),
              users.hashPassword(fData.pass1.get))
            invitations.delete(fData.uuid.get)
            users.addUser(user).map(_ => Redirect(routes.UserCtrl.userHome()).withNewSession.withSession(("userId",user.id.toString)))

          }
          else{
            var form = userForm.fill(fData)
            if ( !uuidExists ) form = form.withError("uuid", "error.invitation.doesNotExist")
            if ( usernameExists ) form = form.withError("username", "error.username.exists")
            if ( emailExists ) form = form.withError("email", "error.email.exists")
            if ( !passwordOK ) form = form.withError("password1", "error.password")
              .withError("password2", "error.password")
            Future(BadRequest(views.html.users.userEditor(form, routes.UserCtrl.doNewUserInvitation, isNew = true,
                                                 )(new AuthenticatedRequest(req, None), messagesProvider)))
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
  
  private def notFound(userId:String) = NotFound("User with username '%s' does not exist.".format(userId))

  def showForgotPassword = Action { implicit req =>
    Ok( views.html.users.forgotPassword(None,None) )
  }

  def doForgotPassword = Action.async{ implicit req =>
    emailForm.bindFromRequest().fold(
      fwi => Future(BadRequest(views.html.users.forgotPassword(None,Some("Error processing forgot password form")))),
      fd => {
        for {
          userOpt <- users.getUserByEmail(fd.email)
          userSessionId = UUID.randomUUID.toString
          emailExists <- userOpt.map(u => forgotPasswords.add(
            PasswordResetRequest(u.username, userSessionId, new Timestamp(System.currentTimeMillis())))
            .map(_=>true))
            .getOrElse(Future(false))
        } yield {
          if ( emailExists ){
            val bodyText = "To reset your password, please click the link below: \n " + conf.get[String]("psps.server.publicUrl") +
              routes.UserCtrl.showResetPassword(userSessionId).url
            val email = Email("Forgot my password", conf.get[String]("play.mailer.user"), Seq(fd.email), bodyText = Some(bodyText))
            mailerClient.send(email)
            val msg = Informational( Informational.Level.Success, Messages("forgotPassword.emailSent", fd.email), "")
            Redirect( routes.UserCtrl.showLogin() ).flashing( FlashKeys.MESSAGE->msg.encoded )
          }
          else {
            BadRequest(views.html.users.forgotPassword(Some(fd.email), Some(Messages("forgotPassword.emailNotFound"))))
          }
        }
      }
    )
  }

  def showResetPassword(requestId:String) = Action.async { implicit req =>
    forgotPasswords.get(requestId).map( {
      case None => NotFound( views.html.errorPage(404, Messages("passwordReset.requestNotFound"), None, None, req, messagesProvider ) )
      case Some(prr) => {
        if ( isRequestExpired(prr) ) {
          forgotPasswords.deleteForUser(requestId)
          Gone(views.html.errorPage(410, Messages("passwordReset.requestExpired"), None, None, req, messagesProvider ))
        } else {
          Ok(views.html.users.passwordReset(prr, None))
        }
      }
    })
    
  }

  def doResetPassword() = Action.async{ implicit req =>
    resetPassForm.bindFromRequest().fold(
      fwi => Future(BadRequest(views.html.users.passwordReset(new PasswordResetRequest("","",null), Some("Error processing reset password form")))),
      fd => {
        for {
          prrOpt      <- forgotPasswords.get(fd.uuid)
          userOpt     <- prrOpt.map(u => users.get(u.username)).getOrElse(Future(None))
          invalidPass =  fd.password1.trim.isEmpty || fd.password1 != fd.password2
          reqExpired  =  prrOpt.exists(u => isRequestExpired(u))
          
        } yield {
          prrOpt match {
            case None => NotFound( views.html.errorPage(404, Messages("passwordReset.requestNotFound"), None, None, req, messagesProvider ) )
            case Some(prr) => {
              if ( invalidPass ) BadRequest(views.html.users.passwordReset(prr, Some(Messages("passwordReset.validationFailed"))))
              else if (reqExpired ) Gone(views.html.errorPage(410, Messages("passwordReset.requestExpired"), None, None, req, messagesProvider ))
              else {
                userOpt match {
                  case None => {
                    // user might have been deleted
                    Gone(views.html.errorPage(410, Messages("passwordReset.requestExpired"), None, None, req, messagesProvider ))
                  }
                  case Some(user) => {
                    // we're OK to reset
                    forgotPasswords.deleteForUser(prr.username)
                    users.updatePassword(user, fd.password1)
                    Redirect(routes.UserCtrl.showLogin()).flashing(FlashKeys.MESSAGE->Messages("passwordReset.success"))
                  }
                }
              }
            }
          }
        }
      }
    )
  }

  def showInviteUser = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    for {
      invitations <- invitations.all
    } yield Ok(views.html.users.inviteUser(invitations))
  }

  def doInviteUser = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    emailForm.bindFromRequest().fold(
      formWithErrors => {
        logger.info( formWithErrors.errors.mkString("\n") )
        for {
          invitations <- invitations.all
        } yield BadRequest(views.html.users.inviteUser(invitations))
      },
      fd => {
        val invitationId = UUID.randomUUID.toString
        invitations.add(Invitation(fd.email, new Timestamp(System.currentTimeMillis()), invitationId, user.email)).map( invite =>{
          sendInvitationEmail(invite)
          val message = Informational(Informational.Level.Success,
                                      Messages("inviteEmail.confirmationMessage"),
                                      Messages("inviteEmail.confirmationDetails",fd.email))
          Redirect(routes.UserCtrl.userHome()).flashing(FlashKeys.MESSAGE->message.encoded)
        })
      }
    )
  }
  
  def apiReInviteUser(invitationUuid:String) = deadbolt.SubjectPresent()(){ implicit req =>
    for {
      invitationOpt <- invitations.get( invitationUuid )
    } yield {
      invitationOpt match {
        case None => NotFound
        case Some(invitation) => sendInvitationEmail(invitation); Ok
      }
    }
  }
  
  def apiDeleteInvitation(invitationUuid:String) = deadbolt.SubjectPresent()() { implicit req =>
    invitations.delete(invitationUuid).map( _ => Ok )
  }
  
  def sendInvitationEmail( invi:Invitation ): Unit = {
    val link = conf.get[String]("psps.server.publicUrl") + routes.UserCtrl.showNewUserInvitation(invi.uuid).url
    val bodyText = Messages("inviteEmail.body", link)
    val email = Email(Messages("inviteEmail.title"), conf.get[String]("play.mailer.user"), Seq(invi.email), Some(bodyText))
    mailerClient.send(email)
    invitations.updateLastSend( invi.uuid, java.time.LocalDateTime.now() )
  }

  def sendSignupEmail( invi:Invitation ): Unit = {
    val link = conf.get[String]("psps.server.publicUrl") + routes.UserCtrl.showNewUserInvitation(invi.uuid).url
    val bodyText = Messages("signup.body", link)
    val email = Email(Messages("signup.title"), conf.get[String]("play.mailer.user"), Seq(invi.email), Some(bodyText))
    mailerClient.send(email)
    invitations.updateLastSend( invi.uuid, java.time.LocalDateTime.now() )
  }
  
  def doChangePassword = deadbolt.SubjectPresent()(){ implicit req =>
    val user = req.subject.get.asInstanceOf[UserSubject].user
    changePassForm.bindFromRequest().fold(
      fwi => {
        Future(BadRequest(views.html.users.userEditor(userForm, routes.UserCtrl.doSaveNewUser, isNew = false)))
      },
      fd => {
        if(users.verifyPassword(user, fd.previousPassword)){
          if (fd.password1.nonEmpty && fd.password1 == fd.password2) {
            users.updatePassword(user, fd.password1).map(_ => {
              val message = Informational(Informational.Level.Success, Messages("password.changed"))
              Redirect(routes.UserCtrl.userHome()).flashing(FlashKeys.MESSAGE->message.encoded)
            })
          } else {
            val form = userForm.fill(UserFormData of user).withError("password1", "error.password")
              .withError("password2", "Passwords must match, and cannot be empty")
            Future(BadRequest(views.html.users.userEditor(form, routes.UserCtrl.doSaveNewUser, isNew = false, activeFirst=false)))
          }
        } else{
          val form = userForm.fill(UserFormData of user).withError("previousPassword", "error.password.incorrect")
          Future(BadRequest( views.html.users.userEditor(form, routes.UserCtrl.doSaveNewUser, isNew=false, activeFirst=false )))
        }
      }
    )
  }
  
  def isRequestExpired( prr:PasswordResetRequest ):Boolean = {
    val oneWeek = 1000 * 60 * 60 * 24 * 7
    val currentTime = System.currentTimeMillis()
    (currentTime - prr.resetPasswordDate.getTime) > oneWeek
  }

  def showSignup() =  Action.async{ implicit req =>
    Future(
      if(!conf.getOptional[Boolean]("AllowSignup").getOrElse(true)) {
        BadRequest(views.html.users.login(loginForm))
    } else {
        Ok( views.html.users.userEditor( userForm, routes.UserCtrl.doSignup, isNew=true
        )(new AuthenticatedRequest(req, None), messagesProvider))
    })
  }


  def doSignup() = Action.async { implicit req =>
    userForm.bindFromRequest().fold(
      formWithErrors => {
        logger.info( formWithErrors.errors.mkString("\n") )
        Future(BadRequest(views.html.users.userEditor(formWithErrors, routes.UserCtrl.doSignup, true)(new AuthenticatedRequest(req, None), messagesProvider))),
      },
      fd => processUserForm( fd, routes.UserCtrl.showLogin, routes.UserCtrl.doSignup, true)(new AuthenticatedRequest(req, None))
    )
  }

  private def processUserForm(fData:UserFormData, onSuccess:Call, onFailure:Call, isNew:Boolean)(implicit req:AuthenticatedRequest[_]):Future[Result] = {
    for {
      usernameExists <- users.usernameExists(fData.username)
      emailExists <- fData.email.map(users.emailExists).getOrElse(Future(false))
      passwordOK = fData.pass1.nonEmpty &&
        fData.pass1.map(_.trim.length).getOrElse(0) > 0 &&
        fData.pass1.map(_.trim) == fData.pass2.map(_.trim)
      canCreateUser = !usernameExists && !emailExists && passwordOK
      res <- if (canCreateUser) attemptUserCreation(fData, onSuccess, onFailure)
      else {
        var form = userForm.fill(fData)
        if (emailExists) form = form.withError("email", "error.email.exists")
        if (usernameExists) form = form.withError("username", "error.username.exists")
        if (!passwordOK) form = form.withError("password1", "error.password")
          .withError("password2", "error.password")
        Future(BadRequest(views.html.users.userEditor(form, onFailure, isNew)))
      }
    } yield res
  }

  private def attemptUserCreation( form:UserFormData, onSuccess:Call, onFailure:Call )(implicit req:AuthenticatedRequest[_]): Future[Result] = {
    val user = User(0, form.username, "", form.email.getOrElse(""), users.hashPassword(form.pass1.get.trim))

    users.tryAddUser(user).map( {
      case Success(user) => Redirect(onSuccess).flashing(FlashKeys.MESSAGE->Informational(Informational.Level.Success, messagesProvider.messages("account.created")).encoded)
      case Failure(exp) => BadRequest( views.html.users.userEditor(userForm.fill(form).withGlobalError(exp.getMessage), onFailure, isNew=true, false))
    } )
  }
}


