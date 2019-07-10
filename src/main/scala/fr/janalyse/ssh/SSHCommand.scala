package fr.janalyse.ssh

import language.implicitConversions

/**
 * SSHCommand class models ssh command
 * @author David Crosson
 */
case class SSHCommand(cmd: String)

/**
 * SSHCommand object implicit conversions container
 * @author David Crosson
 */
object SSHCommand {
  implicit def stringToCommand(cmd: String): SSHCommand = new SSHCommand(cmd)
}
