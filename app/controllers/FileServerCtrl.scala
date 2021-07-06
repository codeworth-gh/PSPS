package controllers

import play.api.Configuration
import play.api.mvc.InjectedController
import play.api.mvc.Results.Ok


import java.nio.file.{Files, Paths}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

/**
  * A controller that serves content from the file system. The content location is configured
  * by `psps.localBaseFolder` in application.conf.
  *
  * This controller handles requests to non-existent files, and blocks
  * request to files outside of the designated content folder.
  *
  * @param conf
  * @param ec
  */
class FileServerCtrl @Inject()(conf:Configuration)(implicit ec:ExecutionContext )
  extends InjectedController {
  
  private val basePath = Paths.get(conf.get[String]("psps.localBaseFolder"))
  
  def get(path:String) = Action{ req =>
    val file =  basePath.resolve(path).normalize()
    if ( file.startsWith(basePath) ) {
      if ( Files.exists(file) ) {
        Ok.sendPath(file)
      } else NotFound("File not found.")
    } else Forbidden("Computer says no.")
  }
}
