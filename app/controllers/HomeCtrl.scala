package controllers

import javax.inject._
import play.api._
import play.api.cache.Cached
import play.api.i18n.{I18nSupport, Langs, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import views.PaginationInfo

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
  
  def pager( currentPage:Int ) = Action{ implicit req =>
    Ok( views.html.pagerSample(generateDataPage(currentPage, 10), PaginationInfo(currentPage, 23)) )
  }
  
  def informationals = Action{ implicit req =>
    Ok( views.html.informationalsSample() )
  }

  def styledInputs = Action{ implicit req =>
    Ok( views.html.styledInputsSample() )
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
          routes.javascript.UserCtrl.apiAddUser,
          routes.javascript.UserCtrl.apiReInviteUser,
          routes.javascript.UserCtrl.apiDeleteInvitation
        )).as("text/javascript")
    }
  }
  
  def notImplYet = TODO
  
  val prefixes   = Seq("","pro","post","pseudo","pre","deep","auto","anti","multi","single","parallel","concurrent")
  val adjectives = Seq("cyber","related","sym/tech","general","dental","argumental","side-channel-ly","persistent")
  val nouns      = Seq("blockchain", "neural network", "css", "browser", "jelly","utopia","shared workspace","lunch","machine learning")
  
  def generateDataPage( pageNum:Int, perPage:Int):Seq[(Int, String)] = {
    val startNum = (pageNum-1)*perPage
    scala.collection.immutable.Range.inclusive(1,perPage).map( i => {
      val itemNum = startNum+i
      val item = Seq(prefixes(itemNum%prefixes.size), adjectives(itemNum%adjectives.size), nouns(itemNum%nouns.size)).mkString(" ")
      (itemNum, item)
    })
  }
  
}
