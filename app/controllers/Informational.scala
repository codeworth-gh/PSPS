package controllers

/**
  * A message to the user. Works with the `Informationals` js library. Values can be encoded
  * as a string, for transporting them to the HTML/JS world.
  *
  * @param level
  * @param title
  * @param subtitle
  */
case class Informational(
                           level:Informational.Level.Value,
                           title:String,
                           subtitle:String
                         ) {
  def encoded:String = Seq(level.toString, title, subtitle).mkString("|")
}

object Informational {
  
  object Level extends Enumeration {
    val Primary   = Value("primary")
    val Secondary = Value("secondary")
    val Success   = Value("success")
    val Danger    = Value("danger")
    val Warning   = Value("warning")
    val Info      = Value("info")
    val Light     = Value("light")
    val Dark      = Value("dark")
  }
  
  def decode(enc:String):Informational = {
    val comps = enc.split("\\|",-1)
    comps.size match {
      case 1 => Informational( Level.Info, comps(0), "")
      case 3 => Informational( Level.withName(comps(0)), comps(1),comps(2) )
      case _ => Informational( Level.Danger, "CANNOT PARSE INFORMATIONAL", enc)
    }
  }

  val defaultTimes:Map[Informational.Level.Value, Int] = Map(
     Level.Success -> 2000,
     Level.Info -> 1700,
     Level.Warning -> 3000,
     Level.Danger ->10000
  ).withDefaultValue(2000)
  
  def apply(level:Level.Value, title:String):Informational = Informational(level, title, "")
}