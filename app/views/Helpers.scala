package views


import play.api.data.{Field, FormError}
import play.api.mvc.Request
import play.api.mvc.Call
import controllers.routes

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
  
  val msg2eng = Map(
    "error.email" -> "Invalid email address",
    "error.minLength" -> "Field cannot be empty"
  )
  
  // TODO this is a quick way of doing this. The proper way would be to use i18n.
  def messageToEng( fe:FormError ):String = msg2eng.getOrElse(fe.message,fe.message)
  
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
      PageSectionItem("Users", routes.UserCtrl.showUserList())
    ))
  )
}