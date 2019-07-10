package fr.janalyse.ssh

import java.io.File
import java.io.{OutputStream,FileOutputStream}

/**
 * TransfertOperations defines generic data transfer operations over SCP or SFTP
 */
trait TransfertOperations extends CommonOperations {
  /**
   * get remote file content as an optional String
   * @param remoteFilename file content to get
   * @return Some content or None if file was not found
   */
  def get(remoteFilename: String): Option[String]

  /**
   * get remote file content as an optional bytes array
   * @param remoteFilename file content to get
   * @return Some content or None if file was not found
   */
  def getBytes(remoteFilename: String): Option[Array[Byte]]

  /**
   * Copy a remote file to a local one
   * @param remoteFilename Source file name (on remote system)
   * @param outputStream Destination stream (local system)
   */
  def receive(remoteFilename: String, outputStream: OutputStream):Unit
  
  /**
   * Copy a remote file to a local one
   * @param remoteFilename Source file name (on remote system)
   * @param toLocalFile Destination file (local system)
   */
  def receive(remoteFilename: String, toLocalFile: File):Unit = {
    receive(remoteFilename, new FileOutputStream(toLocalFile))
  }
  
  /**
   * Copy a remote file to a local one
   * @param remoteFilename Source file name (on remote system)
   * @param localFilename Destination file name (local system)
   */
  def receive(remoteFilename: String, localFilename: String):Unit = {
    receive(remoteFilename, new File(localFilename))
  }
  
  /**
   * Copy and compress (if required) a remote file to a local one
   * @param remoteFilename Source file name (on remote system)
   * @param localFilename Destination filename (without compressed extension)
   * @return local file used
   */
  def receiveNcompress(remoteFilename:String, localFilename:String):File  = {
    val dest = new File(localFilename)
    if (dest.isDirectory()) {
      val basename = remoteFilename.split("/+").last
      receiveNcompress(remoteFilename, dest, basename)
    } else {
      val destdir = Option(dest.getParentFile()).filter(_.exists()).getOrElse(new File("."))
      val basename = dest.getName()
      receiveNcompress(remoteFilename, destdir, basename)
    }
  }
  /**
   * Copy and compress (if required) a remote file to a local one
   * @param remoteFilename Source file name (on remote system)
   * @param localDirectory Destination directory
   * @param localFilename Destination file name (local system), compressed extension may be added to it
   * @return local file used
   */
  def receiveNcompress(remoteFilename:String, localDirectory:File, localBasename:String):File  = {
    val (outputStream, localFile) = if (compressedCheck(remoteFilename).isDefined) {
      //val destfilename = remoteFilename.split("/+").last
      val local  = new File(localDirectory, localBasename)
      val output = new FileOutputStream(local)
      (output, local)
    } else {
      import org.apache.commons.compress.compressors.CompressorStreamFactory
      val destfilename = if (localBasename.endsWith(".gz")) localBasename else localBasename+".gz"
      val local  = new File(localDirectory, destfilename)
      val output = new FileOutputStream(local)      
      val compressedOutput = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, output)
      (compressedOutput, local)
    }
    receive(remoteFilename, outputStream)
    localFile
  }
  private def compressedCheck(filename: String):Option[String] = {
    val GZ = """.*[.]gz$""".r
    val XZ = """.*[.]xz$""".r
    val BZ = """.*[.](?:(?:bz2)|(?:bzip2))""".r
    filename.toLowerCase match {
      case GZ() => Some("gz")
      case BZ() => Some("bz")
      case XZ() => Some("xz")
      case _ => None
    }
  }

  /**
   * Copy a remote file to a local one using the same filename
   * @param filename file name
   */
  def receive(filename: String):Unit = {
    receive(filename, new File(filename))
  }

  /**
   * upload string content to a remote file, if file already exists, it is overwritten
   * @param data content to upload in the remote file
   * @param remoteDestination remote destination
   */
  def put(data: String, remoteDestination: String):Unit

  /**
   * upload bytes array content to a remote file, if file already exists, it is overwritten
   * @param data content to upload in the remote file
   * @param remoteDestination remote destination
   */
  def putBytes(data: Array[Byte], remoteDestination: String):Unit

  /**
   * upload bytes coming from the input stream to a remote file, if file already exists, it is overwritten
   * @param data input stream
   * @param howmany how much data to write to remote destination
   * @param remoteDestination remote destination
   */
  def putFromStream(data: java.io.InputStream, howmany:Int, remoteDestination: String):Unit

  /**
   * Copy a local file to a remote one
   * @param fromLocalFilename Source file name (local system)
   * @param remoteDestination Destination file name (on remote system)
   */
  def send(fromLocalFilename: String, remoteDestination: String):Unit = {
    send(new File(fromLocalFilename), remoteDestination)
  }

  /**
   * Copy a local file to a remote one using the same name
   * @param filename file name
   */
  def send(filename: String):Unit = {
    send(new File(filename), filename)
  }

  /**
   * Copy a local file to a remote one
   * @param fromLocalFile Source file (local system)
   * @param remoteDestination Destination file name (on remote system)
   */
  def send(fromLocalFile: File, remoteDestination: String):Unit

}
