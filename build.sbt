name := "janalyse-ssh"

version := "0.9.12"

organization :="fr.janalyse"

organizationHomepage := Some(new URL("http://www.janalyse.fr"))

scalaVersion := "2.10.3"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

//crossScalaVersions := Seq("2.10.2")

libraryDependencies ++= Seq(
    "com.typesafe"      %% "scalalogging-slf4j" % "1.0.1"
   ,"com.jcraft"         % "jsch"               % "0.1.50"
   ,"org.apache.commons" % "commons-compress"   % "1.7"
   ,"org.scalatest"     %% "scalatest"          % "2.0"    % "test"
   ,"junit"              % "junit"              % "4.11"   % "test"
   ,"com.github.scala-incubator.io" %% "scala-io-core"      % "0.4.2" % "test"
   ,"com.github.scala-incubator.io" %% "scala-io-file"      % "0.4.2" % "test"
)

publishTo := Some(
     Resolver.sftp(
         "JAnalyse Repository",
         "www.janalyse.fr",
         "/home/tomcat/webapps-janalyse/repository"
     ) as("tomcat", new File(util.Properties.userHome+"/.ssh/id_rsa"))
)
