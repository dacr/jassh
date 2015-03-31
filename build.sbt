name := "janalyse-ssh"

version := "0.9.19-SNAPSHOT"

organization :="fr.janalyse"

organizationHomepage := Some(new URL("http://www.janalyse.fr"))

scalaVersion := "2.11.6"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

crossScalaVersions := Seq("2.10.5", "2.11.6")

// Mandatory as tests are also used for performances testing...
parallelExecution in Test := false

libraryDependencies ++= Seq(
    "com.jcraft"         % "jsch"               % "0.1.52"
   ,"org.apache.commons" % "commons-compress"   % "1.9"
   ,"org.slf4j"          % "slf4j-api"          % "1.7.+"
   ,"org.scalatest"     %% "scalatest"          % "2.2.+"  % "test"
   ,"com.github.scala-incubator.io" %% "scala-io-core"      % "0.4.3" % "test"
   ,"com.github.scala-incubator.io" %% "scala-io-file"      % "0.4.3" % "test"
)

publishTo := Some(
     Resolver.sftp(
         "JAnalyse Repository",
         "www.janalyse.fr",
         "/home/tomcat/webapps-janalyse/repository"
     ) as("tomcat", new File(util.Properties.userHome+"/.ssh/id_rsa"))
)

initialCommands in console := """
    |import fr.janalyse.ssh._
    |import java.io.File
    |""".stripMargin
