package views


import java.sql.Timestamp
import java.time.{Instant, LocalDate, LocalDateTime, LocalTime}
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
  
  object DateFmt extends Enumeration {
    val ISO_DateTime: DateFmt.Value = Value
    val ISO_Time: DateFmt.Value = Value
    val ISO_Date: DateFmt.Value = Value
    val HR_DateTime: DateFmt.Value = Value
    val HR_Time: DateFmt.Value = Value
    val HR_Date: DateFmt.Value = Value
  }
  
  import views.Helpers.DateFmt._
  val dateFormats = Map(
    ISO_DateTime -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
    ISO_Time     -> DateTimeFormatter.ofPattern("HH:mm"),
    ISO_Date     -> DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    HR_DateTime  -> DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"),
    HR_Time      -> DateTimeFormatter.ofPattern("HH:mm"),
    HR_Date      -> DateTimeFormatter.ofPattern("dd/MM/yyyy")
  )
  
  def format(fmtName:DateFmt.Value, ldt: LocalDateTime ):String = ldt.format( dateFormats(fmtName) )
  def format(fmtName:DateFmt.Value, ldt: LocalDate ):String = ldt.format( dateFormats(fmtName) )
  def format(fmtName:DateFmt.Value, ldt: LocalTime ):String = ldt.format( dateFormats(fmtName) )
  def format(fmtName:DateFmt.Value, ldt: Timestamp ):String = format( fmtName, LocalDateTime.ofInstant(Instant.ofEpochMilli(ldt.getTime), TimeZone.getDefault.toZoneId))
  
  def encodeUriComponent( s:String ):String = UriEncoding.encodePathSegment(s, java.nio.charset.StandardCharsets.UTF_8)
  def stripHtmlTags(s:String):String = s.replaceAll("<.*?>","")
  
  /**
    * Escape a String s.t. it can be embedded in JS code.
    * @param s The string to be escaped.
    */
  def jsEscape(s:String):String = s.replaceAll("\"", "\\\\\"").replaceAll("\n","\\\\n")
  
  
  def ifNotEmpty(s:String)(block:String=>Html):Html = {
    if ( s!=null && s.trim.nonEmpty ) block(s) else Html("")
  }
  def ifNotEmpty(so:Option[String])(block:String=>Html):Html = so.map(s=>ifNotEmpty(s)(block)).getOrElse(Html(""))
  def ifNotEmpty[T]( col:IterableOnce[T])(block:IterableOnce[T]=>Html):Html = if(col!=null && col.iterator.nonEmpty) block(col) else Html("")
  
  def nonEmptyOrElse(s:String)(nonEmpty:String=>Html)(elseVal:Html):Html = {
    if ( s!=null && s.trim.nonEmpty ) nonEmpty(s) else elseVal
  }
  
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