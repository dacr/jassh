package fr.janalyse.ssh

import language.implicitConversions

/**
 * SSHRemoteFile class models a file on the remote system
 * @author David Crosson
 */
case class SSHRemoteFile(remoteFilename: String) {
  def get(implicit ssh: SSH): Option[String] = {
    ssh.ftp { _ get remoteFilename }
  }
  def put(data: String)(implicit ssh: SSH): Unit = {
    ssh.ftp { _ put (data, remoteFilename) }
  }
  def >>(toLocalFilename: String)(implicit ssh: SSH): Unit = {
    ssh.ftp { _.receive(remoteFilename, toLocalFilename) }
  }
  def <<(fromLocalFilename: String)(implicit ssh: SSH): Unit = {
    ssh.ftp { _.send(fromLocalFilename, remoteFilename) }
  }
}

/**
 * SSHRemoteFile object implicit conversions container
 * @author David Crosson
 */
object SSHRemoteFile {
  implicit def stringToRemoteFile(filename: String):SSHRemoteFile = new SSHRemoteFile(filename)
}
