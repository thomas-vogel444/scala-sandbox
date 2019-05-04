lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion = "2.5.22"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "tom.vogel",
      scalaVersion := "2.12.7"
    )),
    name := "scala-sandbox",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,

      "org.reactivemongo" %% "reactivemongo" % "0.16.5",
      "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % "1.0.0",
      "org.mongodb" % "mongodb-driver-reactivestreams" % "1.11.0",
      "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.1",
      "org.reactivemongo" %% "reactivemongo" % "0.1x",
      "org.reactivemongo" %% "reactivemongo-akkastream" % "0.16.5",

      //      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      //      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.slf4j" % "slf4j-nop" % "1.7.26",
      "commons-codec" % "commons-codec" % "1.9",
      "org.mongodb" % "mongo-java-driver" % "1.3",

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    )
  )
