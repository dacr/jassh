package fr.janalyse.ssh

import org.scalatest.FunSuite

import scala.io.Source
import scala.util.Properties
import org.scalatest.OptionValues._


class SSHConnectionManagerTest extends SomeHelp  {

  test("basic") {
    val lh="127.0.0.1"
    val aps = List(
      AccessPath("test1", SshEndPoint(lh, "test")::Nil),
      AccessPath("test2", SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::Nil),
      AccessPath("test3", SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::Nil),
      AccessPath("test4", ProxyEndPoint(lh,3128)::SshEndPoint(lh, "test")::Nil),
      AccessPath("test5", ProxyEndPoint(lh,3128)::SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::Nil),
      AccessPath("test6", ProxyEndPoint(lh,3128)::SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::Nil),
    )
    val cm = SSHConnectionManager(aps)

    for {
      name <- aps.map(_.name)
    } {
      cm.shell(name) { sh =>
        val msg = s"OK for $name"
        info(s"testing access path $name with message '$msg'")
        sh.execute(s"echo '$msg'").trim should equal(msg)
      }
    }
  }
}
