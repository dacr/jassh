package fr.janalyse.ssh

import org.scalatest.FunSuite

import scala.io.Source
import scala.util.Properties
import org.scalatest.OptionValues._


class SSHConnectionManagerTest extends SomeHelp  {

  ignore("basic") {
    val lh="127.0.0.1"
    val aps = List(
      AccessPath("test1", SshEndPoint(lh, "test")::Nil),
      AccessPath("test2", SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::Nil),
      AccessPath("test3", SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::Nil)
      //AccessPath("test4", ProxyEndPoint(lh,3128)::SshEndPoint(lh, "test")::Nil),
      //AccessPath("test5", ProxyEndPoint(lh,3128)::SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::Nil),
      //AccessPath("test6", ProxyEndPoint(lh,3128)::SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::SshEndPoint(lh, "test")::Nil),
    )
    val cm = SSHConnectionManager(aps)

    def go: Unit = for {
      name <- aps.map(_.name)
    } {
      cm.shell(name) { _.execute("echo 1").trim should equal("1") }
      cm.shell(name) { sh =>
        val msg = s"OK for $name"
        info(s"testing access path $name with message '$msg'")
        sh.execute(s"echo '$msg'").trim should equal(msg)
      }
    }

    info("FIRST")
    go
    info("SECOND")
    go
  }
}
