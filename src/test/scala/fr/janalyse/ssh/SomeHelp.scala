package fr.janalyse.ssh

import org.scalatest.FunSuite
import org.scalatest.ShouldMatchers

trait SomeHelp extends FunSuite with ShouldMatchers {
  val sshopts = SSHOptions("127.0.0.1", username = "test", password = "testtest")

  info(s"Those tests require to have a user named '${sshopts.username}' with password '${sshopts.password}' on ${sshopts.host}")
  
  def f(filename: String) = new java.io.File(filename)

  def now = new java.util.Date()
  
  def howLongFor[T](what: => T) = {
    val begin = System.currentTimeMillis()
    val result = what
    val end = System.currentTimeMillis()
    (end - begin, result)
  }

}
