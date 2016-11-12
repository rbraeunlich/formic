lazy val commonSettings = Seq(
  organization := "de.tu-berlin.formic",
  version := "0.1.0",
  scalaVersion := "2.11.8"
)

val commonDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  // Akka
  "com.typesafe.akka" %% "akka-actor" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11"
)

libraryDependencies ++= commonDependencies

lazy val common = (project in file("common")).
  settings(commonSettings: _*).
  settings(
    name := "formic-common",
    libraryDependencies ++= commonDependencies
  )

lazy val server = (project in file("server")).
  settings(commonSettings: _*).
  settings(
    name := "formic-server",
    libraryDependencies ++= commonDependencies
  ).
  dependsOn(common)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  aggregate(common, server)
