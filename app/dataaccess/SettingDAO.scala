package dataaccess

import javax.inject.Inject
import models.Setting
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.ControllerComponents
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class SettingDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider, cc:ControllerComponents) extends HasDatabaseConfigProvider[JdbcProfile] {
  
  import profile.api._
  private val settingsTable = TableQuery[SettingsTable]
  private implicit val ec:ExecutionContext = cc.executionContext
  
  def all:Future[Map[String, Setting]] = db.run( settingsTable.result ).map( seq => seq.map( r => (r.id, r)).toMap )
  
  def save( aSetting:Setting ):Future[Setting] = db.run {
    settingsTable.insertOrUpdate(aSetting)
  }.map( _ => aSetting )
  
  def get( id:String ):Future[Option[Setting]] = db.run{
    settingsTable.filter( _.id === id ).take(1).result
  }.map( _.headOption )
  
  def apply( id:String ) = get(id)
  
}
