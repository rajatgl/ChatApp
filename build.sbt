import sbt.Fork.scala

name := "ChatApp"

version := "0.1"

scalaVersion := "2.12.2"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(

  //akka essentials
  "com.typesafe.akka" %% "akka-actor" % "2.5.32",
  "com.typesafe.akka" %% "akka-stream" % "2.5.32",
  "com.typesafe.akka" %% "akka-http" % "10.2.2",

  //mongodb dependency
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
  "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "2.0.2",

  //spray-json for marshalling and unmarshalling the data
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.2",

  //jwt dependency
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5",
  "com.nimbusds" % "nimbus-jose-jwt" % "9.3",

  //scala-test dependency
  "org.scalatest" %% "scalatest" % "3.2.2" % "test",
  "org.scalamock" %% "scalamock" % "5.1.0" % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.32",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.2",

  //smtp dependency for mailing
  "com.github.daddykotex" %% "courier" % "3.0.0-M2" ,

  //scala log feature
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2" ,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime ,
  "ch.qos.logback" % "logback-core" % "1.2.3",

  //encryption dependency
  "xyz.wiedenhoeft" %% "scalacrypt" % "0.5-SNAPSHOT"
)