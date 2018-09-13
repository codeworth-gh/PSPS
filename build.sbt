name := """base-app"""

organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  ehcache,
  ws,
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.webjars" % "jquery" % "3.2.1",
  "org.webjars" % "jquery-ui" % "1.12.1",
  "org.webjars" % "tether" % "1.4.0",
  "org.webjars.bower" % "fontawesome" % "4.7.0",
  "be.objectify" %% "deadbolt-scala" % "2.6.1",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.postgresql" % "postgresql" % "42.0.0",
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
  "org.seleniumhq.selenium" % "selenium-java" % "2.35.0" % "test",
  "org.scalamock" %% "scalamock" % "4.0.0" % Test,
  "com.typesafe.play" %% "play-mailer" % "6.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.0"
)

// TODO add sections and table helpers
// TwirlKeys.templateImports ++= Seq( "views.Sections", "views.TableHelper")

LessKeys.compress in Assets := true

includeFilter in (Assets, LessKeys.less) := "*.less"

LessKeys.compress in Assets := true

RjsKeys.modules += WebJs.JS.Object("esversion"->"6")

pipelineStages := Seq(rjs, uglify, digest, gzip)

TwirlKeys.templateImports ++= Seq("views.Helpers")
