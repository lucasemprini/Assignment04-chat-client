name := "assignment04-chat-client"

version := "0.1"

scalaVersion := "2.12.6"

resolvers += "jitpack" at "https://jitpack.io"


scalaSource in Compile := baseDirectory.value / "src/main"

scalaSource in Test := baseDirectory.value / "src/test"

resourceDirectory in Compile := baseDirectory.value / "res"


libraryDependencies += "com.github.etaty" %% "rediscala" % "1.8.0"
libraryDependencies += "com.softwaremill.sttp" %% "core" % "1.2.1"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.13"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.13"
libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.3"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3"
libraryDependencies += "io.vertx" %% "vertx-lang-scala" % "3.5.2"
libraryDependencies += "io.vertx" %% "vertx-web-client-scala" % "3.5.2"
libraryDependencies += "com.github.PlusHaze" % "TrayNotification" % "-SNAPSHOT"
