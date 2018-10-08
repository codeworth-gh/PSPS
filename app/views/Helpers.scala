package views


import java.sql.Timestamp
import java.time.{Instant, LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.util.TimeZone

import play.api.data.{Field, Form, FormError}
import play.api.mvc.Request
import play.api.mvc.Call
import controllers.routes
import play.api.i18n.MessagesProvider
import play.twirl.api.Html
import play.utils.UriEncoding

/**
  * Information required to show a pager component.
  * @param currentPage current page being shown
  * @param pageCount   total number of pages
  */
case class PaginationInfo(currentPage:Int, pageCount:Int )

object Helpers {
  
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  def formatDateTime( ldt: LocalDateTime ):String = ldt.format( dateTimeFormatter )
  def formatDateTime( ldt: Timestamp ):String = formatDateTime( LocalDateTime.ofInstant(Instant.ofEpochMilli(ldt.getTime), TimeZone.getDefault().toZoneId()))
  def formatDate( ldt: LocalDateTime ):String = ldt.format( dateFormatter )
  def formatDate( ldt: LocalDate ):String = ldt.format( dateFormatter )
  
  
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
  
  def formErrors( field:Field )(implicit msgs:MessagesProvider ) = {
    if ( field.hasErrors ) {
      Html(field.errors.flatMap( _.messages ).map( msgs.messages(_) ).mkString("<ul class=\"errors\"><li>","</li><li>","</li></ul>"))
    } else Html("")
  }
  
  def formErrors( form:Form[_] )(implicit msgs:MessagesProvider ) = {
    if ( form.hasGlobalErrors ) {
      Html(form.globalErrors.flatMap( _.messages ).map( msgs.messages(_) ).mkString("<ul class=\"errors\"><li>","</li><li>","</li></ul>"))
    } else Html("")
  }
  
  
}