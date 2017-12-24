import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.kanca",
      scalaVersion := "2.12.4",
      version      := "0.0.0"
    )),
    name := "kanca-api",
    mainClass := Some("KancaApiServer"),
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "com.typesafe.play" %% "play-json" % "2.6.8",
      "com.twitter" %% "finatra-http" % "17.12.0",
      "com.twitter" %% "finatra-http" % "17.12.0" % "test" classifier "tests",
      "com.twitter" %% "inject-app" % "17.12.0" % "test" classifier "tests",
      "com.twitter" %% "inject-core" % "17.12.0" % "test" classifier "tests",
      "com.twitter" %% "inject-modules" % "17.12.0" % "test" classifier "tests",
      "com.twitter" %% "inject-server" % "17.12.0" % "test" classifier "tests",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "mysql" % "mysql-connector-java" % "6.0.6"
    )
  )
