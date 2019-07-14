name := "janalyse-ssh"

assemblyJarName in assembly := "jassh.jar"

organization :="fr.janalyse"
homepage := Some(new URL("https://github.com/dacr/jassh"))

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8", "2.13.0")

// 2.9.3   : generates java 5 bytecodes, even with run with a JVM6
// 2.10.7  : generates java 6 bytecodes
// 2.11.12 : generates java 6 bytecodes
// 2.12.8  : generates java 8 bytecodes && JVM8 required for compilation
// 2.13.0  : generates java 8 bytecodes && JVM8 required for compilation

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

// Mandatory as tests are also used for performances testing...
parallelExecution in Test := false

libraryDependencies ++= Seq(
   "com.jcraft"          %  "jsch"               % "0.1.55",
   "org.apache.commons"  %  "commons-compress"   % "1.18",
   "org.slf4j"           %  "slf4j-api"          % "1.7.26",
   "org.scalatest"       %% "scalatest"          % "3.0.8"  % "test",
   "ch.qos.logback"      %  "logback-classic"    % "1.2.3"  % "test",
)

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, major)) if major >= 13 =>
      Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0" % "test")
    case _ =>
      Seq()
  }
}

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

PgpKeys.useGpg in Global := true      // workaround with pgp and sbt 1.2.x
pgpSecretRing := pgpPublicRing.value  // workaround with pgp and sbt 1.2.x

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
    runClean,
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
 
