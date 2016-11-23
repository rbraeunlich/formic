parallelExecution in Test := false

lazy val root = project
                .enablePlugins(ScalaJSPlugin)
                .in(file(".")).
                  settings(commonSettings: _*).
                  aggregate(commonJS, commonJVM, linearJS, linearJVM, server)

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
      "com.lihaoyi" %%% "upickle" % "0.4.3",
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-actor" % "2.4.11",
      "com.typesafe.akka" %%% "akka-testkit" % "2.4.11"% "test"
    )
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "eu.unicredit" %%% "akkajsactor" % "0.2.4.11"
    )
  )

lazy val commonJVM = common.jvm
lazy val commonJS = common.js

lazy val linear = crossProject.in(file("linear")).
  settings(commonSettings: _*).
  settings(
    name := "formic-linear",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.4.3",
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-testkit" % "2.4.11" % "test"

    )
  )
  .dependsOn(common)

lazy val linearJVM = linear.jvm.dependsOn(commonJVM)
lazy val linearJS = linear.js.dependsOn(commonJS)

lazy val server = (project in file("server")).
  settings(commonSettings: _*).
  settings(
    name := "formic-server",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
      "com.typesafe.akka" %%% "akka-actor" % "2.4.11",
      "com.typesafe.akka" %%% "akka-http-core" % "2.4.11",
      "com.typesafe.akka" %%% "akka-http-experimental" % "2.4.11",
      "com.typesafe.akka" %%% "akka-testkit" % "2.4.11" % "test",
      "com.typesafe.akka" %%% "akka-http-testkit" % "2.4.11" % "test"
    )
  ).
  dependsOn(commonJVM, linearJVM)
