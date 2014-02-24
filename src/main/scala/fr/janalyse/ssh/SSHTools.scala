package fr.janalyse.ssh

import java.io._
import scala.io.BufferedSource

object SSHTools {
  def md5sum(str: String): String = {
    md5sum(new ByteArrayInputStream(str.getBytes())) // TODO : Warning manage charsets...
  }
  def md5sum(input: InputStream): String = {
    val bis = new BufferedInputStream(input)
    val buf = new Array[Byte](1024)
    val md5 = java.security.MessageDigest.getInstance("MD5")
    Stream.continually(bis.read(buf)).takeWhile(_ != -1).foreach(md5.update(buf, 0, _))
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
  }
  def getFile(filename: String): String = {
    new BufferedSource(new FileInputStream(filename)).mkString
  }
  def getRawFile(filename: String): Array[Byte] = {
    inputStream2ByteArray(new FileInputStream(filename))
  }
  def inputStream2ByteArray(input: InputStream): Array[Byte] = {
    val fos = new ByteArrayOutputStream(65535)
    val bfos = new BufferedOutputStream(fos, 16384)
    val bis = new BufferedInputStream(input)
    val buffer = new Array[Byte](8192)
    try {
      Stream.continually(bis.read(buffer))
        .takeWhile(_ != -1)
        .foreach(bfos.write(buffer, 0, _))
    } finally {
      bfos.close
      fos.close
    }
    fos.toByteArray
  }
  def basename(name: String, ext: String) = if (name contains ext) name.substring(0, name.indexOf(ext)) else name

}
