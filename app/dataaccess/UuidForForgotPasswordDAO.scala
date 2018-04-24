package dataaccess

import java.sql.Timestamp
import javax.inject.Inject
import models.UuidForForgotPassword
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UuidForForgotPasswordDAO @Inject() (protected val dbConfigProvider:DatabaseConfigProvider, conf:Configuration) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  private val uuids = TableQuery[UuidForForgotPasswordTable]

  def addUuidForForgotPassword(u: UuidForForgotPassword): Future[UuidForForgotPassword] ={
    db.run{
      uuids += u
    } map ( _ => u )
  }

  // update
  def update( u:UuidForForgotPassword ):Future[UuidForForgotPassword] = {
    db.run {
      uuids.filter( _.username===u.username).update(u)
    } map { _ => u }
  }

  //update uuid for one-time link and date
  def updateOneTimeLinkArgs(u:UuidForForgotPassword, newUuid: String, newDate:Timestamp):Future[UuidForForgotPassword] = {
    update(u.copy(uuid = newUuid, resetPasswordDate = newDate))
  }

  def deleteUuid(u:String): Future[Int] = {
    db.run{
      uuids.filter(_.username === u).delete
    }
  }

  def getUuid(u:String):Future[Option[UuidForForgotPassword]] = {
    db.run{
      uuids.filter(_.username === u).result
    } map { res => res.headOption }
  }

  def getUuidmeByUuid(u:String):Future[Option[UuidForForgotPassword]] = {
    db.run{
      uuids.filter(_.uuid === u).result
    } map {res => res.headOption }
  }
}
