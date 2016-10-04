/*
 * Copyright 2016 David Crosson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import fr.janalyse.ssh.MetaInfo

object JASSHLauncher {
  val greetings = "Using %s release %s (%s)".format(MetaInfo.projectName, MetaInfo.version, MetaInfo.jarbasename)
  def main(args:Array[String]) {
    
    val extendedargs = Array(
       // "-Yrepl-sync",    // Always the same thread used for each REPL evaluated lines
        "-usejavacp",
        "-nocompdaemon",
        "-savecompiled",
        "-deprecation",
        "-Dscala.color"
        ) ++ args
    import util.Properties._
    import scalax.file._
    import scalax.file.ImplicitConversions._

    /*
      The following allow to build an application home directory where to put all scripts
      and addons libraries
    */
    envOrNone("JASSH_HOME") orElse propOrNone("JASSH_HOME") foreach { home =>
      val jars = (home / "lib" ** "*.jar") ++ (home ** "jassh.jar")
      val pathsep = java.io.File.pathSeparator
      val classpath = jars.map(_.toAbsolute.normalize.path).mkString(pathsep)
      System.setProperty("java.class.path", classpath)
    }
    scala.tools.nsc.MainGenericRunner.main(extendedargs)
  }
}
