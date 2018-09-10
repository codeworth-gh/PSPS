package views


import play.api.data.{Field, FormError}
import play.api.mvc.Request
import play.api.mvc.Call
import controllers.routes
import play.twirl.api.Html
import play.utils.UriEncoding

abstract sealed class SectionItem
case class PageSectionItem(title:String, call:Call) extends SectionItem
case object SeparatorSectionItem extends SectionItem

abstract sealed class TopSiteSection[T]{
  def id:T
  def title:String
}

case class PageSection[T](title:String, id:T, call:Call) extends TopSiteSection[T]
case class MultiPageSection[T](title:String, id:T, children:Seq[SectionItem]) extends TopSiteSection[T]

object PublicSections extends Enumeration {
  val Home = Value("Home")
  val Login = Value("Login")
  val Others = Value("Others")
}

object BackOfficeSections extends Enumeration {
  val Home = Value("Home")
  val Users = Value("Users")
}

object Helpers {
  
  def encodeUriComponent( s:String ) = UriEncoding.encodePathSegment(s, java.nio.charset.StandardCharsets.UTF_8)
  def stripHtmlTags(s:String):String = s.replaceAll("<.*?>","")
  
  def ifNotEmpty(s:String)(block:String=>Html):Html = {
    if ( s!=null && s.trim.nonEmpty ) block(s) else Html("")
  }
  def ifNotEmpty(so:Option[String])(block:String=>Html):Html = so.map(s=>ifNotEmpty(s)(block)).getOrElse(Html(""))
  def ifNotEmpty[T]( col:TraversableOnce[T])(block:TraversableOnce[T]=>Html):Html = if(col!=null && col.nonEmpty) block(col) else Html("")
  
  /**
    * Gives a proper css class name based on the field's status. Assumes Bootstrap4.
    * @param f the form field examined.
    * @return css class for the form field (BS4).
    */
  def fieldStatus(f:Field):String = if(f.hasErrors) "has-error" else ""
  
  val publicItems:Seq[TopSiteSection[PublicSections.Value]] = Seq(
    PageSection("Public Home", PublicSections.Home, routes.HomeCtrl.index),
    PageSection("Login", PublicSections.Login, routes.HomeCtrl.index),
    MultiPageSection("Other", PublicSections.Others,
      Seq(
        PageSectionItem("Login", routes.HomeCtrl.index),
        SeparatorSectionItem,
        PageSectionItem("Public Home", routes.HomeCtrl.index)
      )
    )
  )
  
  val backOfficeSections:Seq[TopSiteSection[BackOfficeSections.Value]] = Seq(
    PageSection("BackEnd Home", BackOfficeSections.Home, routes.UserCtrl.userHome() ),
    MultiPageSection("Users", BackOfficeSections.Users, Seq(
      PageSectionItem("Invite Users", routes.UserCtrl.showInviteUser()),
      PageSectionItem("Users", routes.UserCtrl.showUserList()),
      PageSectionItem("Edit Profile", routes.UserCtrl.showNewUserPage())
    ))
  )
}