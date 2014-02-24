/*
 * Copyright 2013 David Crosson
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

package fr.janalyse.ssh

trait CommonOperations {
  
  private def streamMd5sum(input: java.io.InputStream): String = {
    val bis = new java.io.BufferedInputStream(input)
    val buffer = new Array[Byte](1024)
    val md5 = java.security.MessageDigest.getInstance("MD5")
    Stream.continually(bis.read(buffer)).takeWhile(_ != -1).foreach(md5.update(buffer, 0, _))
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft("") { _ + _ }
  }
  private def fileMd5sum(file: java.io.File): String = streamMd5sum(new java.io.FileInputStream(file))
     
  /**
   * locale file md5sum
   * @param filename file name
   * @return md5sum as an optional String, or None if filename was not found
   */
  def localmd5sum(filename: String): Option[String] =
    Option(filename)
       .map(f=> new java.io.File(f))
       .filter(_.exists())
       .map(fileMd5sum _)

}
