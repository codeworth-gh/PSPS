import play.sbt.PlayImport.caffeine

name := """PSPS-baseapp"""

organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  caffeine,
  ws,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.webjars" % "jquery" % "3.2.1",
  "org.webjars" % "jquery-ui" % "1.12.1",
  "org.webjars" % "tether" % "1.4.0",
  "org.webjars.bower" % "fontawesome" % "4.7.0",
  "be.objectify" %% "deadbolt-scala" % "2.7.0",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.postgresql" % "postgresql" % "42.0.0",
  "com.typesafe.play" %% "play-slick" % "4.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2",
  "org.seleniumhq.selenium" % "selenium-java" % "2.35.0" % "test",
  "org.scalamock" %% "scalamock" % "4.0.0" % Test,
  "com.typesafe.play" %% "play-mailer" % "6.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.0",
  "org.webjars" % "sweetalert" % "2.1.0",
  "org.webjars" % "bootstrap" % "4.3.1"
)

// TODO add sections and table helpers
// TwirlKeys.templateImports ++= Seq( "views.Sections", "views.TableHelper")
TwirlKeys.templateImports ++= Seq("views.Helpers")

LessKeys.compress in Assets := true

includeFilter in (Assets, LessKeys.less) := "*.less"

pipelineStages := Seq(rjs, uglify, digest, gzip)

