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
      "com.twitter" %% "finatra-http" % "17.12.0" % Test classifier "tests",
      "com.twitter" %% "inject-app" % "17.12.0",
      "com.twitter" %% "inject-app" % "17.12.0" % Test classifier "tests",
      "com.twitter" %% "inject-core" % "17.12.0",
      "com.twitter" %% "inject-core" % "17.12.0" % Test classifier "tests",
      "com.twitter" %% "inject-modules" % "17.12.0",
      "com.twitter" %% "inject-modules" % "17.12.0" % Test classifier "tests",
      "com.twitter" %% "inject-server" % "17.12.0",
      "com.twitter" %% "inject-server" % "17.12.0" % Test classifier "tests",
      "com.twitter" %% "inject-utils" % "17.12.0",
      "com.twitter" %% "inject-utils" % "17.12.0" % Test,
      "org.mockito" % "mockito-all" % "1.9.5",
      "org.mockito" % "mockito-all" % "1.9.5" % Test,
      "org.specs2" %% "specs2-mock" % "4.0.2",
      "org.specs2" %% "specs2-mock" % "4.0.2" % Test,
      "com.google.inject.extensions" % "guice-testlib" % "4.1.0" % Test,
      "com.google.inject.extensions" % "guice-testlib" % "4.1.0" % Test classifier "tests",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "mysql" % "mysql-connector-java" % "6.0.6"
    )
  )
