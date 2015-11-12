name := """blog"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// Scala 2.10, 2.11
libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"       % "2.2.8",
  "com.h2database"  %  "h2"                % "1.4.189",
  "ch.qos.logback"  %  "logback-classic"   % "1.1.3"
)