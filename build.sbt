val akkaVersion = "2.4.14"

val uPickleVersion = "0.4.3"

val scalatestVersion = "3.0.0"

val akkaHttpVersion = "10.0.0"

val browser = org.scalajs.jsenv.selenium.Firefox()

// Apply to the 'run' command
jsEnv := new org.scalajs.jsenv.selenium.SeleniumJSEnv(browser)

// Apply to tests
jsEnv in Test := new org.scalajs.jsenv.selenium.SeleniumJSEnv(browser)

lazy val root = project
                .enablePlugins(ScalaJSPlugin)
                .in(file(".")).
                  settings(commonSettings: _*).
                  aggregate(commonJS, commonJVM, linearJS, linearJVM, clientJS, clientJVM, server)

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
      "com.lihaoyi" %%% "upickle" % uPickleVersion,
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test"
    )
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "eu.unicredit" %%% "akkajsactor" % ("0." + akkaVersion)
    )
  )

lazy val commonJVM = common.jvm
lazy val commonJS = common.js

lazy val linear = crossProject.in(file("linear")).
  settings(commonSettings: _*).
  settings(
    name := "formic-linear",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % uPickleVersion,
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test"

    )
  )
  .dependsOn(common)

lazy val linearJVM = linear.jvm.dependsOn(commonJVM)
lazy val linearJS = linear.js.dependsOn(commonJS)

lazy val client = crossProject.in(file("client")).
  settings(commonSettings: _*).
  settings(
    name := "formic-client",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % uPickleVersion,
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    )
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "eu.unicredit" %%% "akkajsactor" % ("0." + akkaVersion),
      "eu.unicredit" %%% "akkatestkit" % ("0." + akkaVersion + "-SNAPSHOT"),
      "org.scala-js" %%% "scalajs-dom" % "0.9.0"
    ),
    jsDependencies += RuntimeDOM
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test"
    )
  )
  .dependsOn(common, linear)

lazy val clientJS = client.js.dependsOn(commonJS, linearJS)
lazy val clientJVM = client.jvm.dependsOn(commonJVM, linearJVM)

lazy val server = (project in file("server")).
  settings(commonSettings: _*).
  settings(
    name := "formic-server",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test",
      "com.typesafe.akka" %%% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %%% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %%% "akka-http-testkit" % akkaHttpVersion % "test"
    )
  ).
  dependsOn(commonJVM, linearJVM)
