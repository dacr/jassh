package fr.janalyse.ssh

object Main {
  def main(args:Array[String]) {
    
    val extendedargs = Array(
        "-Yrepl-sync",    // Always the same thread used for each REPL evaluated lines
        "-usejavacp",
        "-nocompdaemon",
        "-savecompiled",
        "-deprecation"
        ) ++ args

    scala.tools.nsc.MainGenericRunner.main(extendedargs)
  }
}
