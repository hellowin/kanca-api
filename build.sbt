import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.kanca",
      scalaVersion := "2.12.4",
      version      := "0.0.0"
    )),
    name := "kanca-api",
    mainClass := Some("KancaApiApp"),
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "com.typesafe.play" %% "play-json" % "2.6.8"
    )
  )
