package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import play.api.Configuration
import play.api.mvc.InjectedController
import play.api.mvc.Results.Ok
import storage.KmlFileMetadataStore

import java.nio.file.{Files, Paths}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class FileServerCtrl @Inject() (conf:Configuration)( implicit ec:ExecutionContext )
  extends InjectedController {
  
  private val basePath = Paths.get(conf.get[String]("ecfdb.localBaseFolder"))
  
  def get(path:String) = Action{ req =>
    val file =  basePath.resolve(path).normalize()
    if ( file.startsWith(basePath) ) {
      if ( Files.exists(file) ) {
        Ok.sendPath(file)
      } else NotFound("File not found.")
    } else Forbidden("Computer says no.")
  }
}
