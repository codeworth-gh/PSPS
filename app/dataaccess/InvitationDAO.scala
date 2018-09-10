package dataaccess

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

  def addUuid(i: Invitation): Future[Invitation] = {
    db.run(
      (invitations returning invitations).insertOrUpdate(i)
    ).map( insertRes => insertRes.getOrElse(i) )
  }

  def deleteUuid(u: String): Unit = {
    db.run {
      invitations.filter(_.uuid === u).delete
    }
  }

  def uuidExists(u: String): Future[Boolean] = {
    db.run {
      invitations.map(_.uuid).filter(_ === u).exists.result
    }
  }
  
  def all:Future[Seq[Invitation]] = db.run( invitations.result )
}

