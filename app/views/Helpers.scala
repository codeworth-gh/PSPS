package views


import play.api.data.{Field, FormError}
import play.api.mvc.Request
import play.api.mvc.Call
import controllers.routes

abstract sealed class SectionItem
case class PageSectionItem(title:String, call:Call) extends SectionItem
case object SeparatorSectionItem extends SectionItem

abstract sealed class TopSiteSection {
  def title:String
  def id:String
}
case class PageSection(title:String, id:String, call:Call) extends TopSiteSection
case class MultiPageSection(title:String, id:String, children:Seq[SectionItem]) extends TopSiteSection

object Helpers {
  val msg2eng = Map(
    "error.email" -> "Invalid email address",
    "error.minLength" -> "Field cannot be empty"
  )
  
  // TODO this is a quick way of doing this. The proper way would be to use i18n.
  def messageToEng( fe:FormError ):String = msg2eng.getOrElse(fe.message,fe.message)
  
  def fieldStatus(f:Field):String = if(f.hasErrors) "has-error" else ""
  
  val SEC_1 = "sec1"
  val SEC_2 = "sec2"
  val SEC_3 = "sec3"
  
  val publicItems = Seq(
    PageSection("Public Home", SEC_1, routes.HomeCtrl.index),
    PageSection("Login", SEC_2, routes.HomeCtrl.index),
    MultiPageSection("Other", SEC_3,
      Seq(
        PageSectionItem("Login", routes.HomeCtrl.index),
        SeparatorSectionItem,
        PageSectionItem("Public Home", routes.HomeCtrl.index)
      )
    )
  )
}