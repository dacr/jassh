package fr.janalyse.ssh

import org.scalatest.FunSuite
import org.scalatest.Matchers

trait SomeHelp extends FunSuite with Matchers {
  val defaultUsername = "test"
  val defaultPassword = "testtest"
  val defaultHost = "127.0.0.1"
  val sshopts = SSHOptions(defaultHost, username = defaultUsername, password = defaultPassword)

  info(s"Those tests require to have a user named '${sshopts.username}' with password '${sshopts.password}' on ${sshopts.host}")
  
  def f(filename: String) = new java.io.File(filename)

  def now = new java.util.Date()
  
  def howLongFor[T](what: => T): (Long, T) = {
    val begin = System.currentTimeMillis()
    val result = what
    val end = System.currentTimeMillis()
    (end - begin, result)
  }

}
