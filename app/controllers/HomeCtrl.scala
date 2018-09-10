package controllers

import javax.inject._
import play.api._
import play.api.cache.Cached
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */

class HomeCtrl @Inject()(cc: ControllerComponents, cached: Cached) extends AbstractController(cc) {
  
  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.publicIndex())
  }
  
  def sampleNavbar = Action { implicit req =>
    Ok(views.html.sampleNavbar("A message"))
  }
  
  def apiSayHi = Action { implicit req => Ok("Hi!") }
  
  /**
    * Routes for the front-end.
    * @return
    */
  def frontEndRoutes = cached("ACT_frontEndRoutes") {
    Action { implicit request =>
      Ok(
        routing.JavaScriptReverseRouter("feRoutes")(
          routes.javascript.HomeCtrl.apiSayHi
        )).as("text/javascript")
    }
  }
  
  /**
    * Routes for the front-end.
    * @return
    */
  def backEndRoutes = cached("ACT_backEndRoutes") {
    Action { implicit request =>
      Ok(
        routing.JavaScriptReverseRouter("beRoutes")(
          routes.javascript.HomeCtrl.apiSayHi
        )).as("text/javascript")
    }
  }
  
  def notImplYet = TODO
  
  
}
