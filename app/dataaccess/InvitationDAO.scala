package dataaccess

import javax.inject.Inject
import models.Invitation
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class InvitationDAO @Inject() (protected val dbConfigProvider:DatabaseConfigProvider, conf:Configuration) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  private val uuids = TableQuery[InvitationTable]

  def addUuid(u: Invitation): Future[Invitation] = {
    db.run {
      uuids += u
    } map (_ => u)
  }

  def deleteUuid(u: String): Unit = {
    db.run {
      uuids.filter(_.uuid === u).delete
    }
  }

  def uuidExists(u: String): Future[Boolean] = {
    db.run {
      uuids.map(_.uuid).filter(_ === u).exists.result
    }
  }
}