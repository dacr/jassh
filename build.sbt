name := "janalyse-ssh"

assemblyJarName in assembly := "jassh.jar"

organization :="fr.janalyse"
homepage := Some(new URL("https://github.com/dacr/jassh"))

scalaVersion := "2.11.8"
scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")
crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1")

// Mandatory as tests are also used for performances testing...
parallelExecution in Test := false

libraryDependencies ++= Seq(
    "com.jcraft"         % "jsch"               % "0.1.54"
   ,"org.apache.commons" % "commons-compress"   % "1.13"
   ,"org.slf4j"          % "slf4j-api"          % "1.7.25"
   ,"org.scalatest"     %% "scalatest"          % "3.0.1"  % "test"
)

initialCommands in console := """
    |import fr.janalyse.ssh._
    |import java.io.File
    |""".stripMargin

test in assembly := {}

artifactName in assembly := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
     artifact.name + "." + artifact.extension
}

