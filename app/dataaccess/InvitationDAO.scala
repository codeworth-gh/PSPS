package dataaccess

import java.sql.Timestamp
import java.time.LocalDateTime

import javax.inject.Inject
import models.Invitation
import play.api.{Configuration, Logger}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class InvitationDAO @Inject() (protected val dbConfigProvider:DatabaseConfigProvider, conf:Configuration) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  private val invitations = TableQuery[InvitationTable]

  def add(i: Invitation): Future[Invitation] = {
    db.run(
      (invitations returning invitations).insertOrUpdate(i)
    ).map( insertRes => insertRes.getOrElse(i) )
  }

  def delete(uuid: String): Future[Int] = {
    db.run {
      invitations.filter(_.uuid === uuid).delete
    }
  }

  def exists(uuid: String): Future[Boolean] = {
    db.run {
      invitations.map(_.uuid).filter(_ === uuid).exists.result
    }
  }
  
  def get( uuid:String ):Future[Option[Invitation]] = db.run(
    invitations.filter( _.uuid === uuid).result
    ).map( _.headOption )
  
  def updateLastSend( uuid:String, date:LocalDateTime ):Future[Int] = {
    val ts = Timestamp.valueOf(date)
    db.run {
      invitations.filter(_.uuid === uuid).map(_.date).update(ts)
    }
  }
  
  def all:Future[Seq[Invitation]] = db.run( invitations.result )
}

