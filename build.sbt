name                   := "janalyse-ssh"
organization           := "fr.janalyse"
homepage               := Some(new URL("https://github.com/dacr/jassh"))
licenses += "Apache 2" -> url(s"http://www.apache.org/licenses/LICENSE-2.0.txt")
scmInfo                := Some(ScmInfo(url(s"https://github.com/dacr/jassh"), s"git@github.com:dacr/jassh.git"))

scalaVersion := "3.3.1"

crossScalaVersions := Seq("2.13.12", "3.3.1")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

// Mandatory as tests are also used for performances testing...
Test / parallelExecution := false

libraryDependencies ++= Seq(
  //   "com.jcraft"          %  "jsch"               % "0.1.55", // no longer maintained
  "com.github.mwiede"       % "jsch"                       % "0.2.13", // drop-in replacement with enhancements
  "org.apache.commons"      % "commons-compress"           % "1.25.0",
  "org.slf4j"               % "slf4j-api"                  % "2.0.9",
  "org.scalatest"          %% "scalatest"                  % "3.2.17" % Test,
  "ch.qos.logback"          % "logback-classic"            % "1.4.11" % Test,
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4" % Test
)

console / initialCommands :=
  """
    |import fr.janalyse.ssh._
    |import java.io.File
    |""".stripMargin
