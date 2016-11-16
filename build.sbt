lazy val root = project
                .enablePlugins(ScalaJSPlugin)
                .in(file(".")).
                  settings(commonSettings: _*).
                  aggregate(commonJS, commonJVM, server)

lazy val commonSettings = Seq(
  organization := "de.tu-berlin.formic",
  version := "0.1.0",
  scalaVersion := "2.11.8"
)

lazy val common = crossProject.in(file("common")).
  settings(commonSettings: _*).
  settings(
    name := "formic-common",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "io.spray" %%  "spray-json" % "1.3.2",
      "com.typesafe.akka" %%% "akka-actor" % "2.4.11"
    )
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "eu.unicredit" %%% "akkajsactor" % "0.2.4.11"
    )
  )

lazy val commonJVM = common.jvm
lazy val commonJS = common.js


lazy val server = (project in file("server")).
  settings(commonSettings: _*).
  settings(
    name := "formic-server",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
      "com.typesafe.akka" %%% "akka-actor" % "2.4.11",
      "com.typesafe.akka" %%% "akka-http-core" % "2.4.11",
      "com.typesafe.akka" %%% "akka-http-experimental" % "2.4.11"
    )
  ).
  dependsOn(commonJVM)
