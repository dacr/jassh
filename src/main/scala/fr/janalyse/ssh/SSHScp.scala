package fr.janalyse.ssh

import java.io._
import com.jcraft.jsch.{ChannelExec}



class SSHScp(implicit ssh: SSH) extends TransfertOperations {

  override def get(remoteFilename: String): Option[String] = {
    getBytes(remoteFilename).map(new String(_, ssh.options.charset))
  }

  override def getBytes(remoteFilename: String): Option[Array[Byte]] = {
    var filesBuffer = Map.empty[String, ByteArrayOutputStream]
    def filename2outputStream(filename: String) = {
      val newout = new ByteArrayOutputStream()
      filesBuffer += filename -> newout
      newout
    }
    remoteFile2OutputStream(remoteFilename, filename2outputStream) match {
      case 0 => None
      case 1 => Some(filesBuffer.values.head.toByteArray)
      case _ => throw new RuntimeException("Want one file, but several files were found ! (%s)".format(filesBuffer.keys.mkString(",")))
    }
  }

  override def receive(remoteFilename: String, outputStream: OutputStream) {
    def filename2outputStream(filename: String) = outputStream // just One file supported
    remoteFile2OutputStream(remoteFilename, filename2outputStream) match {
      case 0 => throw new RuntimeException("Remote file name '%s' not found".format(remoteFilename))
      case 1 => // OK
      case _ => throw new RuntimeException("Want one file, but several files were found for '%s'".format(remoteFilename))
    }
  }

  override def put(data: String, remoteDestination: String) {
    putBytes(data.getBytes(ssh.options.charset), remoteDestination)
  }

  override def putBytes(data: Array[Byte], remoteDestination: String) {
    val sz = data.length
    val linput = new ByteArrayInputStream(data)
    val parts = remoteDestination.split("/")
    val rfilename = parts.last
    val rDirectory = if (parts.init.size == 0) "." else parts.init.mkString("/")

    inputStream2remoteFile(linput, sz, rfilename, rDirectory)
  }

  override def putFromStream(data: java.io.InputStream, howmany:Int, remoteDestination: String) {
    val parts = remoteDestination.split("/")
    val rfilename = parts.last
    val rDirectory = if (parts.init.size == 0) "." else parts.init.mkString("/")

    inputStream2remoteFile(data, howmany, rfilename, rDirectory)
    
  }
  
  override def send(fromLocalFile: File, remoteDestination: String) {
    val sz = fromLocalFile.length
    val linput = new FileInputStream(fromLocalFile)
    val parts = remoteDestination.split("/", -1)
    val rfilename = if (parts.last.length == 0) fromLocalFile.getName else parts.last
    val rDirectory = if (parts.init.size == 0) "." else parts.init.mkString("/")

    inputStream2remoteFile(linput, sz, rfilename, rDirectory)
  }

  /**
   * upload a local input stream to a remote destination
   * @param localinput the input stream from which we read data
   * @param datasize amount of data to send (in bytes)
   * @param remoteFilename remote file name to use (just a filename, not a path, shouln't contain any path separator)
   * @param remoteDirectory remote destination directory for our file
   */

  def inputStream2remoteFile(
    localinput: InputStream,
    datasize: Long,
    remoteFilename: String,
    remoteDirectory: String) {
    val ch = ssh.jschsession.openChannel("exec").asInstanceOf[ChannelExec]
    try {
      ch.setCommand("""scp -p -t "%s"""".format(remoteDirectory))
      val sin = new BufferedInputStream(ch.getInputStream())
      val sout = ch.getOutputStream()
      ch.connect(ssh.options.connectTimeout.toInt)

      checkAck(sin)

      // send "C0644 filesize filename", where filename should not include '/'
      //println("******"+remoteFilename+" "+remoteDirectory)
      val command = "C0644 %d %s\n".format(datasize, remoteFilename) // TODO take into account remote file rights
      sout.write(command.getBytes("US-ASCII"))
      sout.flush()

      checkAck(sin)

      val bis = new BufferedInputStream(localinput)
      /*
      val chk = {
        var readCount=0L
        (x:Int) => {
          readCount+=1
          readCount <= datasize & x >= 0
        }
      }
      if (datasize>0) Stream.continually(bis.read()).takeWhile(chk(_)).foreach(sout.write(_))
      */
      var writtenBytes = 0
      while (writtenBytes < datasize) {
        val c = bis.read()
        if (c >= 0) {
          sout.write(c)
          writtenBytes += 1
        }
      }
      bis.close()

      // send '\0'
      sout.write(Array[Byte](0x00))
      sout.flush()

      checkAck(sin)

    } finally {
      if (ch.isConnected) ch.disconnect
    }
  }

  /**
   * lookup for remote files, for each found file send the content to
   * an OutputStream created using the specified builder
   * @param remoteFilenameMask file name or file mask
   * @return number of found files
   */

  def remoteFile2OutputStream(
    remoteFilenameMask: String,
    outputStreamBuilder: (String) => OutputStream): Int = {
    val ch = ssh.jschsession.openChannel("exec").asInstanceOf[ChannelExec]
    try {
      ch.setCommand("""scp -f "%s"""".format(remoteFilenameMask))
      val sin = new BufferedInputStream(ch.getInputStream())
      val sout = ch.getOutputStream()
      ch.connect(ssh.options.connectTimeout.toInt)

      sout.write(0)
      sout.flush()

      var count = 0
      val buf = new StringBuilder() // Warning : Mutable state, take care
      def bufAppend(x: Int) { buf.append(x.asInstanceOf[Char]) }
      def bufReset() { buf.setLength(0) }
      def bufStr = buf.toString

      while (checkAck(sin) == 'C') {
        val fileRights = new Array[Byte](5)
        sin.read(fileRights, 0, 5)

        bufReset()
        Stream.continually(sin.read()).takeWhile(_ != ' ').foreach(bufAppend(_))
        val fz = bufStr.toLong

        bufReset()
        Stream.continually(sin.read()).takeWhile(_ != 0x0a).foreach(bufAppend(_))
        val filename = bufStr

        //println(remoteFilenameMask+ " " + count + " " + new String(fileRights)+ " '"+ filename + "' #" + fz)

        sout.write(0)
        sout.flush()

        val fos = new BufferedOutputStream(outputStreamBuilder(filename), 8192)

        /*
        val chk = {
          var readCount=0L
          (x:Int) => {
            readCount+=1
            readCount <= fz && x >= 0
          }
        }
        if (fz>0) Stream.continually(sin.read()).takeWhile(chk(_)).foreach(fos.write(_))
        */

        var writtenBytes = 0L
        while (writtenBytes < fz) {
          val c = sin.read()
          if (c >= 0) {
            fos.write(c)
            writtenBytes += 1
          }
        }

        fos.close

        count += 1

        checkAck(sin)
        sout.write(0)
        sout.flush()
      }

      count
    } finally {
      if (ch.isConnected) ch.disconnect
    }
  }

  private def checkAck(in: InputStream): Int = {
    def consumeMessage() = {
      val sb = new StringBuffer()
      Stream.continually(in.read())
        .takeWhile(x => (x != '\n') && (x != -1))
        .foreach(x => sb.append(x.asInstanceOf[Char]))
    }
    in.read() match {
      case 1 => throw new RuntimeException("SSH transfert protocol error " + consumeMessage())
      case 2 => throw new RuntimeException("SSH transfert protocol fatal error " + consumeMessage())
      case x => x
    }
  }

  def close() {}

}
