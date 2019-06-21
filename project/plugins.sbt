addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")


resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.eed3si9n"       %   "sbt-unidoc"              % "0.4.2")
addSbtPlugin("com.github.gseitz"  %   "sbt-release"             % "1.0.11")
addSbtPlugin("com.jsuereth"       %   "sbt-pgp"                 % "1.1.1")
addSbtPlugin("org.scoverage"      %   "sbt-scoverage"           % "1.6.0")
addSbtPlugin("com.typesafe.sbt"   %   "sbt-ghpages"             % "0.6.3")
addSbtPlugin("org.xerial.sbt"     %   "sbt-sonatype"            % "2.5")
addSbtPlugin("com.codacy"         %   "sbt-codacy-coverage"     % "1.3.15")
