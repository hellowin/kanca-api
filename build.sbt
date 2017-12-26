import Dependencies._

val versions = new {
  val twitter = "17.12.0"
  val mockito = "1.9.5"
  val specs2 = "4.0.2"
  val guice = "4.1.0"
  val logback = "1.2.3"
  val mysqlConnector = "6.0.6"
  val play = "2.6.8"
  val scalajHttp = "2.3.0"
}

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.kanca",
      scalaVersion := "2.12.4",
      version := "0.0.0"
    )),
    name := "kanca-api",
    mainClass := Some("KancaApiServer"),
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.scalaj" %% "scalaj-http" % versions.scalajHttp,
      "com.typesafe.play" %% "play-json" % versions.play,
      "com.twitter" %% "finatra-http" % versions.twitter,
      "com.twitter" %% "finatra-http" % versions.twitter % Test classifier "tests",
      "com.twitter" %% "inject-app" % versions.twitter,
      "com.twitter" %% "inject-app" % versions.twitter % Test classifier "tests",
      "com.twitter" %% "inject-core" % versions.twitter,
      "com.twitter" %% "inject-core" % versions.twitter % Test classifier "tests",
      "com.twitter" %% "inject-modules" % versions.twitter,
      "com.twitter" %% "inject-modules" % versions.twitter % Test classifier "tests",
      "com.twitter" %% "inject-server" % versions.twitter,
      "com.twitter" %% "inject-server" % versions.twitter % Test classifier "tests",
      "com.twitter" %% "inject-utils" % versions.twitter,
      "com.twitter" %% "inject-utils" % versions.twitter % Test,
      "org.mockito" % "mockito-all" % versions.mockito,
      "org.mockito" % "mockito-all" % versions.mockito % Test,
      "org.specs2" %% "specs2-mock" % versions.specs2,
      "org.specs2" %% "specs2-mock" % versions.specs2 % Test,
      "com.google.inject.extensions" % "guice-testlib" % versions.guice % Test,
      "com.google.inject.extensions" % "guice-testlib" % versions.guice % Test classifier "tests",
      "ch.qos.logback" % "logback-classic" % versions.logback,
      "mysql" % "mysql-connector-java" % versions.mysqlConnector
    ),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )
