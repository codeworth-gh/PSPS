package dataaccess

import java.sql.Timestamp
import javax.inject.Inject
import models.PasswordResetRequest
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PasswordResetRequestDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider, conf:Configuration) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  private val requests = TableQuery[PasswordResetRequestTable]

  def add(u: PasswordResetRequest): Future[PasswordResetRequest] ={
    db.run{
      requests += u
    } map ( _ => u )
  }

  // update
  def update( u:PasswordResetRequest ):Future[PasswordResetRequest] = {
    db.run {
      requests.filter( _.username===u.username).update(u)
    } map { _ => u }
  }

  // update uuid for one-time link and date
  def updateOneTimeLinkArgs(u:PasswordResetRequest, newUuid: String, newDate:Timestamp):Future[PasswordResetRequest] = {
    update(u.copy(uuid = newUuid, resetPasswordDate = newDate))
  }

  def deleteForUser(username:String): Future[Int] = {
    db.run{
      requests.filter(_.username === username).delete
    }
  }

  def get(uuid:String):Future[Option[PasswordResetRequest]] = {
    db.run{
      requests.filter(_.uuid === uuid).result
    } map { res => res.headOption }
  }
  
}
