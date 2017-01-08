val akkaVersion = "2.4.16"

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
    treeJS,
    treeJVM,
    jsonJS,
    jsonJVM,
    clientJS,
    clientJVM,
    websockettestsJS,
    server,
    exampleJS,
    exampleJVM
  ).disablePlugins(AssemblyPlugin)

lazy val commonSettings = Seq(
  organization := "de.tu-berlin.formic",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  test in assembly := {}
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
    parallelExecution in Test := false,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %%% "akka-persistence" % akkaVersion,
      "commons-io" % "commons-io" % "2.4"
)
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "org.akka-js" %%% "akkajsactor" % ("0." + akkaVersion)
    )
  )

lazy val commonJVM = common.jvm.disablePlugins(AssemblyPlugin)
lazy val commonJS = common.js.disablePlugins(AssemblyPlugin)

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
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "org.akka-js" %%% "akkajstestkit" % ("0." + akkaVersion)
    )
  )
  .dependsOn(common)

lazy val linearJVM = linear.jvm.dependsOn(commonJVM).disablePlugins(AssemblyPlugin)
lazy val linearJS = linear.js.dependsOn(commonJS).disablePlugins(AssemblyPlugin)

lazy val tree = crossProject.in(file("tree")).
  settings(commonSettings: _*).
  settings(
    name := "formic-tree",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % uPickleVersion,
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test"
    )
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "org.akka-js" %%% "akkajstestkit" % ("0." + akkaVersion)
    )
  )
  .dependsOn(common)

lazy val treeJVM = tree.jvm.dependsOn(commonJVM).disablePlugins(AssemblyPlugin)
lazy val treeJS = tree.js.dependsOn(commonJS).disablePlugins(AssemblyPlugin)

lazy val json = crossProject.in(file("json")).
  settings(commonSettings: _*).
  settings(
    name := "formic-json",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % uPickleVersion,
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test"
    )
  ).
  jsSettings(
    libraryDependencies ++= Seq(
      "org.akka-js" %%% "akkajstestkit" % ("0." + akkaVersion)
    )
  )
  .dependsOn(common, tree)

lazy val jsonJVM = json.jvm.dependsOn(commonJVM, treeJVM).disablePlugins(AssemblyPlugin)
lazy val jsonJS = json.js.dependsOn(commonJS, treeJS).disablePlugins(AssemblyPlugin)

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
      "org.akka-js" %%% "akkajsactor" % ("0." + akkaVersion),
      "org.akka-js" %%% "akkajstestkit" % ("0." + akkaVersion),
      "org.scala-js" %%% "scalajs-dom" % "0.9.0"
    )
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test"
    )
  )
  .dependsOn(common, linear, tree, json)

lazy val clientJS = client.js.dependsOn(commonJS, linearJS, treeJS, jsonJS).disablePlugins(AssemblyPlugin)
lazy val clientJVM = client.jvm.dependsOn(commonJVM, linearJVM, treeJVM, jsonJVM).disablePlugins(AssemblyPlugin)

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
    jsEnv in Test := new org.scalajs.jsenv.RetryingComJSEnv(PhantomJSEnv().value, 10)
  )
  .dependsOn(common, linear, client)

lazy val websockettestsJS = websockettests.js.dependsOn(commonJS, linearJS, clientJS).disablePlugins(AssemblyPlugin)

lazy val server = (project in file("server")).
  settings(commonSettings: _*).
  settings(
    fork := true,
    parallelExecution in Test := false,
    name := "formic-server",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test",
      "com.typesafe.akka" %%% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %%% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %%% "akka-http-testkit" % akkaHttpVersion % "test",
      "org.iq80.leveldb"            % "leveldb"          % "0.7",
      "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8"
)
  ).
  dependsOn(commonJVM, linearJVM, treeJVM, jsonJVM).disablePlugins(AssemblyPlugin)

lazy val example = crossProject.in(file("example")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
  ).
  jvmSettings(
    fork := true,
    parallelExecution in Test := false,
    mainClass in assembly := Some("de.tu_berlin.formic.example.ExampleServer"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %%% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %%% "akka-http-testkit" % akkaHttpVersion % "test",
      "org.seleniumhq.selenium" % "selenium-java" % "3.0.1" % "test"
    )
  ).
  jsSettings(
    libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
    jsDependencies += "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js",
    jsDependencies += RuntimeDOM,
    persistLauncher := true,
    skip in packageJSDependencies := false
  ).
  dependsOn(client, common)

lazy val exampleJS = example.js.dependsOn(commonJS, linearJS, treeJS, clientJS).disablePlugins(AssemblyPlugin)
lazy val exampleJVM = example.jvm.settings(
  (resources in Compile) += (fastOptJS in (exampleJS, Compile)).value.data,
  (resources in Compile) += (packageJSDependencies in (exampleJS, Compile)).value,
  (resources in Compile) += (packageScalaJSLauncher in (exampleJS, Compile)).value.data
).dependsOn(commonJVM, linearJVM, clientJVM, treeJVM, jsonJVM, server)