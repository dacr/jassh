package fr.janalyse.ssh

import com.jcraft.jsch.{ChannelSftp, SftpException, SftpATTRS}
import java.io._
import java.nio.charset.Charset
import scala.io.BufferedSource
import collection.JavaConverters._

object SSHFtp {
  /**
   * A representation of the entries in a directory listing.
   */
  case class LsEntry(filename: String, longname: String, attrs: SftpATTRS)
}

class SSHFtp(implicit ssh: SSH) extends TransfertOperations with SSHLazyLogging {
  private val channel: ChannelSftp = {
    //jschftpchannel.connect(link.connectTimeout)
    val ch = ssh.jschsession().openChannel("sftp").asInstanceOf[ChannelSftp]
    ch.connect(ssh.options.connectTimeout.toInt)
    ch
  }

  def close() = {
    channel.quit
    channel.disconnect
  }

  override def get(filename: String): Option[String] = {
    try {
      implicit val codec = new scala.io.Codec(Charset.forName(ssh.options.charset))
      Some(new BufferedSource(channel.get(filename)).mkString)
    } catch {
      case e: SftpException if (e.id == 2) => None // File doesn't exist
      case e: IOException => None
    }
  }

  override def getBytes(filename: String): Option[Array[Byte]] = {
    try {
      Some(SSHTools.inputStream2ByteArray(channel.get(filename)))
    } catch {
      case e: SftpException if (e.id == 2) => None // File doesn't exist
      case e: IOException => None
    }
  }

  /**
    * get remote file content as an optional InputStream
    * @param filename file content to get
    * @return Some content or None if file was not found
    */
  def getStream(filename: String): Option[InputStream] = {
    try {
      Some(channel.get(filename))
    } catch {
      case e: SftpException if e.id == 2 => None // File doesn't exist
      case e: IOException => None
    }
  }

  /**
   * Rename a remote file or directory
   * @param origin Original remote file name
   * @param dest Destination (new) remote file name
   */
  def rename(origin: String, dest: String) = {
    channel.rename(origin, dest)
  }

  /**
   * List contents of a remote directory
   * @param path The path of the directory on the remote system
   */
  def ls(path: String): List[SSHFtp.LsEntry] =
    channel.ls(path)
      .asScala
      .map(_.asInstanceOf[channel.LsEntry])
      .map(entry => SSHFtp.LsEntry(entry.getFilename, entry.getLongname, entry.getAttrs))
      .toList

  /*
   * Remove a file from a remote system
   * @param path The path of the remote file to remove
   */
  def rm(path: String): Unit = channel.rm(path)

  /**
   * Remove a directory from a remote system
   * @param path The path of the directory to remove
   */
  def rmdir(path: String): Unit = channel.rmdir(path)

  /**
   * Creates a directory on a remote system
   * @param path The name of the directory to create
   */
  def mkdir(path: String): Unit = channel.mkdir(path)

  /**
   * Change the working directory on the remote system
   * @param path The new working directory, relative to the current one
   */
  def cd(path: String): Unit = channel.cd(path)

  override def receive(remoteFilename: String, outputStream: OutputStream) {
    try {
      channel.get(remoteFilename, outputStream)
    } catch {
      case e: SftpException if (e.id == 2) =>
        logger.warn(s"File '${remoteFilename}' doesn't exist")
        throw e
      case e: IOException =>
        logger.error(s"can't receive ${remoteFilename}", e)
        throw e
      case e: Exception =>
        logger.error(s"can't receive ${remoteFilename}", e)
        throw e
    } finally {
      outputStream.close
    }
  }

  override def put(data: String, remoteFilename: String) {
    channel.put(new ByteArrayInputStream(data.getBytes(ssh.options.charset)), remoteFilename)
  }

  override def putBytes(data: Array[Byte], remoteFilename: String) {
    channel.put(new ByteArrayInputStream(data), remoteFilename)
  }

  override def putFromStream(data: java.io.InputStream, howmany:Int, remoteDestination: String) {
    putFromStream(data, remoteDestination) // In that case howmany is ignored !
  }
  
  def putFromStream(data: java.io.InputStream, remoteDestination: String) {
    channel.put(data, remoteDestination)
  }
  
  override def send(localFile: File, remoteFilename: String) {
    channel.put(new FileInputStream(localFile), remoteFilename)
  }

}
