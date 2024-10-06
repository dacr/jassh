name         := "janalyse-ssh"
organization := "fr.janalyse"
description  := "High level scala SSH API for easy and fast operations on remote servers."

licenses += "Apache 2" -> url(s"http://www.apache.org/licenses/LICENSE-2.0.txt")

scalaVersion       := "3.5.1"
crossScalaVersions := Seq("2.13.15", "3.5.1")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  //   "com.jcraft"          %  "jsch"               % "0.1.55", // no longer maintained
  "com.github.mwiede"       % "jsch"                       % "0.2.20", // drop-in replacement with enhancements
  "org.apache.commons"      % "commons-compress"           % "1.27.1",
  "org.slf4j"               % "slf4j-api"                  % "2.0.16",
  "org.scalatest"          %% "scalatest"                  % "3.2.19" % Test,
  "ch.qos.logback"          % "logback-classic"            % "1.5.8"  % Test,
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"  % Test
)

// Mandatory as tests are also used for performances testing...
Test / parallelExecution := false

console / initialCommands :=
  """
    |import fr.janalyse.ssh._
    |import java.io.File
    |""".stripMargin

homepage   := Some(url("https://github.com/dacr/jassh"))
scmInfo    := Some(ScmInfo(url(s"https://github.com/dacr/jassh"), s"git@github.com:dacr/jassh.git"))
developers := List(
  Developer(
    id = "dacr",
    name = "David Crosson",
    email = "crosson.david@gmail.com",
    url = url("https://github.com/dacr")
  )
)
