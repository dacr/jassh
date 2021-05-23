package fr.janalyse.ssh

import org.scalatest.funsuite._
import org.scalatest.matchers.should

trait SomeHelp extends AnyFunSuite with should.Matchers {
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
