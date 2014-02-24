import AssemblyKeys._

seq(assemblySettings: _*)

name := "janalyse-ssh-onejar"

scalaVersion := "2.10.2"

mainClass in assembly := Some("fr.janalyse.ssh.Main")

jarName in assembly := "jassh.jar"

libraryDependencies <++=  scalaVersion { sv =>
       ("org.scala-lang" % "jline"           % sv  % "compile")  ::
       ("org.scala-lang" % "scala-compiler"  % sv  % "compile")  ::
       ("org.scala-lang" % "scalap"          % sv  % "compile")  ::Nil
}

libraryDependencies += "fr.janalyse"   %% "janalyse-ssh" % "0.9.11" % "compile"

resolvers += "JAnalyse Repository" at "http://www.janalyse.fr/repository/"


// jansi is embedded inside jline !
excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
  cp filter {c=> List("jansi") exists {c.data.getName contains _} }
}
