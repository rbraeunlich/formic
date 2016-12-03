val akkaVersion = "2.4.14"

val uPickleVersion = "0.4.3"

val scalatestVersion = "3.0.0"

val akkaHttpVersion = "10.0.0"

lazy val root = project
  .enablePlugins(ScalaJSPlugin)
  .in(file(".")).
  settings(commonSettings: _*).
  aggregate(commonJS,
    commonJVM,
    linearJS,
    linearJVM,
    clientJS,
    clientJVM,
    websockettestsJS,
    websockettestsJVM,
    server,
    example
  )

lazy val commonSettings = Seq(
  organization := "de.tu-berlin.formic",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
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
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test"
    )
  )
  .dependsOn(common, linear)

lazy val clientJS = client.js.dependsOn(commonJS, linearJS)
lazy val clientJVM = client.jvm.dependsOn(commonJVM, linearJVM)

//PhantomJS and AkkaJSTestkit do not work together, so they have to be split
lazy val websockettests = crossProject.in(file("websockettests")).
  settings(commonSettings: _*).
  settings(
    name := "formic-websockettests",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    )
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.0"
    ),
    jsEnv in Test := PhantomJSEnv().value
  )
  .dependsOn(common, linear, client)

lazy val websockettestsJS = websockettests.js.dependsOn(commonJS, linearJS, clientJS)
lazy val websockettestsJVM = websockettests.jvm.dependsOn(commonJVM, linearJVM, clientJVM)

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

lazy val example = project.in(file("example")).
  settings(commonSettings: _*).
  settings(
    name := "formic-example-app",
    persistLauncher := true,
    skip in packageJSDependencies := false,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    jsDependencies += RuntimeDOM
  ).
  dependsOn(commonJS, linearJS, clientJS).
  enablePlugins(ScalaJSPlugin)
