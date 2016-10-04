name := "janalyse-ssh-onejar"

scalaVersion := "2.11.8"

mainClass in assembly := Some("JASSHLauncher")

jarName in assembly := "jassh.jar"

libraryDependencies ++= Seq(
  "fr.janalyse"                   %% "janalyse-ssh"  % "+"
 ,"com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3"
 ,"com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3"
// ,"org.scala-lang"                 % "jline"         % "2.11"
)


libraryDependencies <++=  scalaVersion { sv =>
       ("org.scala-lang" % "scala-compiler"  % sv  % "compile")  ::
       ("org.scala-lang" % "scalap"          % sv  % "compile")  ::Nil
}

resolvers += "JAnalyse Repository" at "http://www.janalyse.fr/repository/"

// jansi is embedded inside jline !
excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {c=> List("jansi") exists {c.data.getName contains _} }
  }

// ------------------------------------------------------
sourceGenerators in Compile <+=
 (sourceManaged in Compile, version, name, jarName in assembly) map {
  (dir, version, projectname, jarexe) =>
  val file = dir / "fr" / "janalyse" / "ssh" / "MetaInfo.scala"
  IO.write(file,
  """package fr.janalyse.ssh
    |object MetaInfo {
    |  val version="%s"
    |  val projectName="%s"
    |  val jarbasename="%s"
    |}
    |""".stripMargin.format(version, projectname, jarexe.split("[.]").head) )
  Seq(file)
}
