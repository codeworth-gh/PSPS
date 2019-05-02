package modules

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import dataaccess.UsersDAO
import javax.inject.Inject
import models.User
import play.api.mvc.ControllerComponents

import scala.concurrent.Future

class UserService @Inject()(users: UsersDAO, cc:ControllerComponents) extends IdentityService[User] {
  implicit private val ec = cc.executionContext
  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = users.get(loginInfo.providerKey)
  def retrieve(id:Long) = users.get(id)
  def save(user:User) = users.addUser(user)
}