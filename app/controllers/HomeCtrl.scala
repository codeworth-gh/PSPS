package controllers

import javax.inject._
import play.api._
import play.api.cache.Cached
import play.api.i18n.{I18nSupport, Langs, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */

class HomeCtrl @Inject()(langs: Langs, messagesApi: MessagesApi, cached: Cached, cc: ControllerComponents
                        ) extends AbstractController(cc) with I18nSupport {
  
//  implicit val mApiImplicit = messagesApi
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
    Ok(views.html.sampleNavbar("Parametrized Message"))
  }
  
  def apiSayHi = Action(cc.parsers.tolerantJson) { implicit req =>
    val json = req.body.as[JsObject]
    val name = json("name")
    Ok(Json.obj("message"->s"Hello, $name."))
  }
  
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
