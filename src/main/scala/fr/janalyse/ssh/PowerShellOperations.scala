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

import com.typesafe.scalalogging.LazyLogging
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.generic.CanBuildFrom
import java.util.Locale
import scala.util.matching.Regex

/**
 * ShellOperations defines generic shell operations and common shell commands shortcuts
 */
trait PowerShellOperations extends LazyLogging {

  /**
   * Execute the current command and return the result as a string
   * @param cmd command to be executed
   * @return result string
   */
  def execute(cmd: SSHCommand): String

  /**
   * Execute the current command and return the result as a trimmed string
   * @param cmd command to be executed
   * @return result string
   */
  def executeAndTrim(cmd: SSHCommand): String = execute(cmd).trim()

  /**
   * Execute the current command and return the result as a trimmed splitted string
   * @param cmd command to be executed
   * @return result string
   */
  def executeAndTrimSplit(cmd: SSHCommand): Iterable[String] = execute(cmd).trim().split("\r?\n")



  /**
   * who am I ?
   * @return current user name
   */
  def whoami: String = executeAndTrim("whoami")

  /**
   * List files in specified directory
   * @return current directory files as an Iterable
   */
  def ls(): String = execute("ls")

  /**
   * List files in specified directory
   * @param dirname directory to look into
   * @return current directory files as an Iterable
   */
  def ls(dirname: String): Iterable[String] = {
    //executeAndTrimSplit("""ls --format=single-column "%s" """.format(dirname))
    executeAndTrimSplit("""ls "%s" """.format(dirname)).filter(_.size > 0)
  }

  /**
   * Get current working directory
   * @return current directory
   */
  def pwd(): String = executeAndTrim("pwd")

  /**
   * Change current working directory to home directory
   * Of course this requires a persistent shell session to be really useful...
   */
  def cd { execute("cd") }

  /**
   * Change current working directory to the specified directory
   * Of course this requires a persistent shell session to be really useful...
   * @param dirname directory name
   */
  def cd(dirname: String) { execute(s"""cd "$dirname" """) }

  /**
   * Get remote host name
   * @return host name
   */
  def hostname: String = executeAndTrim("""hostname""")

  /**
   * Get remote date, as a java class Date instance (minimal resolution = 1 second)
   * Note PowerShell returns %Z as "-05" but  Java expects "-0500"
   * @return The remote system current date as a java Date class instance
   */
  def date(): Date = {
    val d = executeAndTrim("date -u '+%Y-%m-%d %H:%M:%S %Z00'")
    dateSDF.parse(d)
  }
  private lazy val dateSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")

  /**
   * Get the content of a file
   * @param filename get the content of this filename
   * @return file content
   */
  def cat(filename: String) = execute("cat %s".format(filename))

  /**
   * Get contents of a list of files
   * @param filenames get the content of this list of filenames
   * @return files contents concatenation
   */
  def cat(filenames: List[String]) = execute("cat %s".format(filenames.mkString(" ")))

  /**
   * get current SSH options
   * @return used ssh options
   */
  def options: SSHOptions


  /**
   * kill specified processes
   */
  def kill(pids: Iterable[Int]) { execute(s"""kill -9 ${pids.mkString(" ")}""") }

  /**
   * delete a file
   */
  def rm(file: String) { rm(file::Nil) }

  /**
   * delete files
   */
  def rm(files: Iterable[String]) { execute(s"""rm -f ${files.mkString("'", "' '", "'")}""") }

  /**
   * delete directory (directory must be empty)
   */
  def rmdir(dir: String) { rmdir(dir::Nil)}

  /**
   * delete directories (directories must be empty)
   */
  def rmdir(dirs: Iterable[String]) { execute(s"""rmdir ${dirs.mkString("'", "' '", "'")}""") }

}
