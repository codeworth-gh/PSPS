package controllers

object InformationalLevel extends Enumeration {
  val Primary   = Value("primary")
  val Secondary = Value("secondary")
  val Success   = Value("success")
  val Danger    = Value("danger")
  val Warning   = Value("warning")
  val Info      = Value("info")
  val Light     = Value("light")
  val Dark      = Value("dark")
}

/**
  * A message to the user. Works with the `Informationals` js library. Values can be encoded
  * as a string, for transporting them to the HTML/JS world.
  *
  * @param level
  * @param title
  * @param subtitle
  */
case class Informational(
                           level:InformationalLevel.Value,
                           title:String,
                           subtitle:String
                         ) {
  def encoded = Seq(level.toString(), title, subtitle).mkString("|")
}

object Informational {
  def decode(enc:String) = {
    val comps = enc.split("\\|",-1)
    comps.size match {
      case 1 => Informational( InformationalLevel.Info, comps(0), "")
      case 3 => Informational( InformationalLevel.withName(comps(0)), comps(1),comps(2) )
      case _ => Informational( InformationalLevel.Danger, "CANNOT PARSE INFORMATIONAL", enc)
    }
  }
   def defaultTimes = Map(InformationalLevel.Success -> 2000,
     InformationalLevel.Info -> 1700,
     InformationalLevel.Warning -> 3000,
     InformationalLevel.Danger ->10000
   )
  
  def apply(level:InformationalLevel.Value, title:String):Informational = Informational(level, title, "")
}