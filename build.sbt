name := "janalyse-ssh"
organization :="fr.janalyse"
homepage := Some(new URL("https://github.com/dacr/jassh"))
licenses += "Apache 2" -> url(s"http://www.apache.org/licenses/LICENSE-2.0.txt")
scmInfo := Some(ScmInfo(url(s"https://github.com/dacr/jassh"), s"git@github.com:dacr/jassh.git"))

scalaVersion := "3.0.0"

crossScalaVersions := Seq("2.13.6", "3.0.0")
// 2.9.x  : generates java 5 bytecodes, even with run with a JVM6
// 2.10.x : generates java 6 bytecodes
// 2.11.x : generates java 6 bytecodes
// 2.12.x : generates java 8 bytecodes && JVM8 required for compilation
// 2.13.x : generates java 8 bytecodes && JVM8 required for compilation

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

// Mandatory as tests are also used for performances testing...
Test / parallelExecution := false

libraryDependencies ++= Seq(
   "com.jcraft"          %  "jsch"               % "0.1.55",
   "org.apache.commons"  %  "commons-compress"   % "1.20",
   "org.slf4j"           %  "slf4j-api"          % "1.7.30",
   "org.scalatest"       %% "scalatest"          % "3.2.9"  % Test,
   "ch.qos.logback"      %  "logback-classic"    % "1.2.3"  % Test,
   "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3" % Test
)

console / initialCommands := """
    |import fr.janalyse.ssh._
    |import java.io.File
    |""".stripMargin
