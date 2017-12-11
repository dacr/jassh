name := "janalyse-ssh"

assemblyJarName in assembly := "jassh.jar"

organization :="fr.janalyse"
homepage := Some(new URL("https://github.com/dacr/jassh"))

//scalaVersion := "2.12.3"
scalaVersion := "2.11.12"
scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")
crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.3")

// Mandatory as tests are also used for performances testing...
parallelExecution in Test := false

libraryDependencies ++= Seq(
    "com.jcraft"          %  "jsch"               % "0.1.54"
   ,"org.apache.commons"  %  "commons-compress"   % "1.14"
   ,"org.slf4j"           %  "slf4j-api"          % "1.7.25"
   ,"io.github.andrebeat" %% "scala-pool"         % "0.4.0"
   ,"org.scalatest"       %% "scalatest"          % "3.0.4"  % "test"
   ,"ch.qos.logback"      %  "logback-classic"    % "1.2.3"  % "test"

)

initialCommands in console := """
    |import fr.janalyse.ssh._
    |import java.io.File
    |""".stripMargin

test in assembly := {}

artifactName in assembly := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
     artifact.name + "." + artifact.extension
}



pomIncludeRepository := { _ => false }

useGpg := true

licenses += "Apache 2" -> url(s"http://www.apache.org/licenses/LICENSE-2.0.txt")
releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle := true
publishArtifact in Test := false
publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)

scmInfo := Some(ScmInfo(url(s"https://github.com/dacr/jassh"), s"git@github.com:dacr/jassh.git"))

pomExtra in Global := {
  <developers>
    <developer>
      <id>dacr</id>
      <name>David Crosson</name>
      <url>https://github.com/dacr</url>
    </developer>
  </developers>
}


import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    //runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
 
