/*
 * Copyright 2014 David Crosson
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

import scala.io.Source
import scala.util.Properties
import java.io.File
import java.io.IOException
import org.scalatest.OptionValues._

import scala.collection.parallel.immutable.ParVector

class SSHAPITest extends SomeHelp {

  //==========================================================================================================
  test("One line exec with automatic resource close") {
    SSH.once(sshopts) { _.execute("expr 1 + 1").trim } should equal("2")
    SSH.once(sshopts) { _.executeAndTrim("expr 1 + 1") } should equal("2")
    //SSH.once(sshopts) { _.execute("echo 1" :: "echo 2" :: Nil) }.map(_.trim) should equal("1" :: "2" :: Nil)
    val year = SSH.once(sshopts) { _.executeAndTrim("expr 1 + 10").toInt }
    year should equal(11)
  }
  //==========================================================================================================
  test("Execution & file transferts within the same ssh session") {
    SSH.once(sshopts) { ssh =>
      val rfile = "HelloWorld.txt"
      val lfile = "/tmp/sshtest.txt"
      def clean = {
        f(rfile).delete
        f(lfile).delete
      }
      clean

      val msg = ssh execute "echo -n 'Hello %s'".format(util.Properties.userName)

      ssh.put(msg, rfile)

      (ssh get rfile) should equal(Some(msg))

      ssh.receive(rfile, lfile)

      Source.fromFile(lfile).getLines().next() should equal(msg)
    }
  }

  //==========================================================================================================
  test("Execution & file transferts within the same sh & ftp persisted session") {
    SSH.shellAndFtp(sshopts) { (sh, ftp) =>
      val rfile = "HelloWorld.txt"
      val lfile = "/tmp/sshtest.txt"
      def clean = {
        f(rfile).delete
        f(lfile).delete
      }
      clean

      val msg = sh execute "echo -n 'Hello %s'".format(util.Properties.userName)

      ftp.put(msg, rfile)

      (ftp get rfile) should equal(Some(msg))

      ftp.receive(rfile, lfile)

      Source.fromFile(lfile).getLines().next() should equal(msg)
    }
  }

  //==========================================================================================================
  test("shell coherency check") {
    SSH.shell(sshopts) { sh =>
      (1 to 100) foreach { i =>
        sh.executeAndTrim("echo ta" + i) should equal("ta" + i)
        sh.executeAndTrim("echo ga" + i) should equal("ga" + i)
      }
    }
  }

  //==========================================================================================================
  ignore("shell coherency check with long command lines (in //)") {
    SSH.once(sshopts) { ssh =>
      (ParVector()++(1 to 10)) foreach { i =>
        ssh.shell { sh =>
          def mkmsg(base: String) = base * 100 + i
          sh.executeAndTrim("echo %s".format(mkmsg("Z"))) shouldBe mkmsg("Z")
          sh.executeAndTrim("echo %s".format(mkmsg("ga"))) shouldBe mkmsg("ga")
          sh.executeAndTrim("echo %s".format(mkmsg("PXY"))) shouldBe mkmsg("PXY")
          sh.executeAndTrim("echo %s".format(mkmsg("GLoups"))) shouldBe mkmsg("GLoups")
        }
      }
    }
  }
  //==========================================================================================================
  test("SSHShell : Bad performances obtained without persistent schell ssh channel (autoclose)") {
    val howmany = 200
    for {
      (opts, comment) <- (sshopts, "") :: (sshopts.copy(execWithPty = true), "with VTY") :: Nil
    } {
      SSH.once(opts) { ssh =>
        val (dur, _) = howLongFor {
          for (i <- 1 to howmany) { ssh.shell(_ execute "ls -d /tmp && echo 'done'") }
        }
        val throughput = howmany.doubleValue() / dur * 1000
        info(f"Performance using shell without channel persistency : $throughput%.1f cmd/s $comment")
      }
    }
  }
  //==========================================================================================================
  test("SSHShell : Best performance is achieved with mutiple command within the same shell channel (autoclose)") {
    val howmany = 5000
    for {
      (opts, comment) <- (sshopts, "") :: (sshopts.copy(execWithPty = true), "with VTY") :: Nil
    } {
      SSH.once(opts) {
        _.shell { sh =>
          val (dur, _) = howLongFor {
            for (i <- 1 to howmany) { sh execute "ls -d /tmp && echo 'done'" }
          }
          val throughput = howmany.doubleValue() / dur * 1000
          info(f"Performance using with channel persistency : $throughput%.1f cmd/s $comment%s")
        }
      }
    }
  }
  //==========================================================================================================
  test("SSHExec : performances obtained using exec ssh channel (no persistency)") {
    val howmany = 200
    for {
      (opts, comment) <- (sshopts, "") :: (sshopts.copy(execWithPty = true), "with VTY") :: Nil
    } {
      SSH.once(opts) { ssh =>
        val (dur, _) = howLongFor {
          for (i <- 1 to howmany) { ssh execOnce "ls -d /tmp && echo 'done'" }
        }
        val throughput = howmany.doubleValue() / dur * 1000
        info(f"Performance using exec ssh channel (no persistency) : $throughput%.1f cmd/s $comment")
      }
    }
  }
  //==========================================================================================================
  test("Start a remote process in background") {
    import fr.janalyse.ssh.SSH
    SSH.once(sshopts) { ssh =>

      var x = List.empty[String]

      def receiver(result: ExecResult):Unit= { result match { case ExecPart(d) => x = x :+ d case _ => } }
      val executor = ssh.run("for i in 1 2 3 4 5 ; do echo hello$1 ; done", receiver)

      executor.waitForEnd

      x.zipWithIndex map { case (l, i) => info("%d : %s".format(i, l)) }
      x.size should equal(5)
    }
  }

  //==========================================================================================================
  test("Usage case example - for tutorial") {
    import fr.janalyse.ssh.SSH
    SSH.once(sshopts) { ssh =>

      val uname = ssh executeAndTrim "uname -a"
      val fsstatus = ssh execute "df -m"
      val fmax = ssh get "/etc/lsb-release" // Warning SCP only work with regular file

      ssh.shell { sh => // For higher performances
        val hostname = sh.executeAndTrim("hostname")
        val files = sh.execute("find /usr/lib/")
      }
      ssh.ftp { ftp => // For higher performances
        val cpuinfo = ftp.get("/proc/cpuinfo")
        val meminfo = ftp.get("/proc/meminfo")
      }
      // output streaming
      def receiver(result: ExecResult):Unit = { result match { case ExecPart(m) => info(s"received :$m") case _ => } }
      val executor = ssh.run("for i in 1 2 3 ; do echo hello$i ; done", receiver)
      executor.waitForEnd
    }
  }

  //==========================================================================================================
  test("Simultaenous SSH operations") {
    val started = System.currentTimeMillis()
    val cnxinfos = ParVector(sshopts, sshopts, sshopts, sshopts, sshopts)
    val sshs = cnxinfos map { SSH(_) }

    //sshs.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(6))

    val unames = sshs map { ssh => scala.concurrent.blocking {ssh.execute("date; sleep 5") }}
    info(unames.mkString("----"))

    (System.currentTimeMillis() - started) should be < (8000L) //(and not 5s * 5 = 25s)
  }

  //==========================================================================================================
  test("Simplified persistent ssh shell usage") {
    SSH.shell(defaultHost, defaultUsername, defaultPassword) { sh =>
      sh.execute("ls -la")
      sh.execute("uname")
    }
  }

  //==========================================================================================================
  test("Simplified persistent ssh shell and ftp usage") {
    SSH.shellAndFtp(sshopts) { (sh, ftp) =>
      sh.execute("ls")
      sh.execute("uname")
      ftp.get("/proc/stat")
      ftp.get("/proc/vmstat")
    }
  }

  //==========================================================================================================
  test("simplified usage with sshOptions as Option") { // TODO - not OK for Darwin
    val cnxinfo = Some(sshopts)
    val stat = SSH.once(cnxinfo) { _.get("/dev/null") }.flatten

    stat should not equal (None)

    stat.get.size should equal(0)
  }

  //==========================================================================================================
  test("file transfert performances (with content loaded in memory)") {
    val testfile = "test-transfert"

    def withSCP(filename: String, ssh: SSH, howmany: Int, sizeKb: Int):Unit = {
      for (_ <- 1 to howmany)
        ssh.getBytes(filename).map(_.length) should equal(Some(sizeKb * 1024))
    }
    def withSFTP(filename: String, ssh: SSH, howmany: Int, sizeKb: Int):Unit = {
      for (_ <- 1 to howmany)
        ssh.ftp(_.getBytes(filename)).map(_.length) should equal(Some(sizeKb * 1024))
    }
    def withReusedSFTP(filename: String, ssh: SSH, howmany: Int, sizeKb: Int):Unit = {
      ssh.ftp { ftp =>
        for (_ <- 1 to howmany)
          ftp.getBytes(filename).map(_.length) should equal(Some(sizeKb * 1024))
      }
    }

    def toTest(thattest: (String, SSH, Int, Int) => Unit,
               howmany: Int,
               sizeKb: Int,
               comments: String)(ssh: SSH) = {
      ssh.execute("dd count=%d bs=1024 if=/dev/zero of=%s".format(sizeKb, testfile))
      val (d, _) = howLongFor {
        thattest(testfile, ssh, howmany, sizeKb)
      }
      info("Bytes rate : %.1fMb/s %dMb in %.1fs for %d files - %s".format(howmany * sizeKb * 1000L / d / 1024d, sizeKb * howmany / 1024, d / 1000d, howmany, comments))
    }

    val withCipher = sshopts.copy(noneCipher = false)
    val noneCipher = sshopts.copy(noneCipher = true)

    SSH.once(withCipher)(toTest(withSCP, 3, 10 * 1024, "byterates using SCP"))
    SSH.once(noneCipher)(toTest(withSCP, 3, 10 * 1024, "byterates using SCP (with none cipher)"))
    SSH.once(withCipher)(toTest(withSFTP, 3, 10 * 1024, "byterates using SFTP"))
    SSH.once(noneCipher)(toTest(withSFTP, 3, 10 * 1024, "byterates using SFTP (with none cipher)"))
    SSH.once(withCipher)(toTest(withReusedSFTP, 3, 10 * 1024, "byterates using SFTP (session reused"))
    SSH.once(noneCipher)(toTest(withReusedSFTP, 3, 10 * 1024, "byterates using SFTP (session reused, with none cipher)"))

    SSH.once(withCipher)(toTest(withSCP, 100, 1024, "byterates using SCP"))
    SSH.once(noneCipher)(toTest(withSCP, 100, 1024, "byterates using SCP (with none cipher)"))
    SSH.once(withCipher)(toTest(withSFTP, 100, 1024, "byterates using SFTP"))
    SSH.once(noneCipher)(toTest(withSFTP, 100, 1024, "byterates using SFTP (with none cipher)"))
    SSH.once(withCipher)(toTest(withReusedSFTP, 100, 1024, "byterates using SFTP (session reused)"))
    SSH.once(noneCipher)(toTest(withReusedSFTP, 100, 1024, "byterates using SFTP (session reused, with none cipher)"))
  }

  //==========================================================================================================
  test("ssh compression") {
    val testfile = "test-transfert"

    def withSCP(filename: String, ssh: SSH, howmany: Int, sizeKb: Int):Unit = {
      for (_ <- 1 to howmany)
        ssh.getBytes(filename).map(_.length) shouldBe Some(sizeKb * 1024)
    }
    def withSFTP(filename: String, ssh: SSH, howmany: Int, sizeKb: Int):Unit = {
      for (_ <- 1 to howmany)
        ssh.ftp(_.getBytes(filename)).map(_.length) shouldBe Some(sizeKb * 1024)
    }
    def withReusedSFTP(filename: String, ssh: SSH, howmany: Int, sizeKb: Int):Unit = {
      ssh.ftp { ftp =>
        for (_ <- 1 to howmany)
          ftp.getBytes(filename).map(_.length) shouldBe Some(sizeKb * 1024)
      }
    }

    def toTest(thattest: (String, SSH, Int, Int) => Unit,
               howmany: Int,
               sizeKb: Int,
               comments: String)(ssh: SSH):Unit = {
      ssh.execute("dd count=%d bs=1024 if=/dev/zero of=%s".format(sizeKb, testfile))
      val (d, _) = howLongFor {
        thattest(testfile, ssh, howmany, sizeKb)
      }
      info("Bytes rate : %.1fMb/s %dMb in %.1fs for %d files - %s".format(howmany * sizeKb * 1000L / d / 1024d, sizeKb * howmany / 1024, d / 1000d, howmany, comments))
    }

    val withCompress = sshopts.copy(compress = None)
    val noCompress = sshopts.copy(compress = Some(9))

    SSH.once(withCompress)(toTest(withReusedSFTP, 1, 100 * 1024, "byterates using SFTP (max compression)"))
    SSH.once(noCompress)(toTest(withReusedSFTP, 1, 100 * 1024, "byterates using SFTP (no compression)"))
  }

  //==========================================================================================================
  test("tunneling test remote->local") {
    SSH.once(defaultHost, defaultUsername, defaultPassword, port = 22) { ssh1 =>
      ssh1.remote2Local(22022, defaultHost, 22)
      SSH.once(defaultHost, defaultUsername, defaultPassword, port = 22022) { ssh2 =>
        ssh2.executeAndTrim("echo 'works'") should equal("works")
      }
    }
  }

  //==========================================================================================================
  test("tunneling test local->remote") {
    SSH.once(defaultHost, defaultUsername, defaultPassword, port = 22) { ssh1 =>
      ssh1.local2Remote(33033, defaultHost, 22)
      SSH.once(defaultHost, defaultUsername, defaultPassword, port = 33033) { ssh2 =>
        ssh2.executeAndTrim("echo 'works'") should equal("works")
      }
    }
  }

  //==========================================================================================================
  test("tunneling test intricated tunnels") {
    // From host/port, bring back locally remote fhost/fport to local host using tport. 
    case class Sub(host: String, port: Int, fhost: String, fport: Int, tport: Int)

    // We simulate bouncing between 9 SSH hosts, using SSH tunnel intrication
    // A:22 -> B:22 -> C:22 -> D:22 -> E:22 -> F:22 -> G:22 -> H:22 -> I:22
    // All "foreign" hosts become directly accessible using new ssh local ports
    // A->10022, B-> 10023, ... Z->10030, so now I (and all others) are direcly accessible from local ssh client host
    val intricatedPath = Iterable(
      Sub("localhost", 22, "127.0.0.1", 22, 10022), // A 
      Sub("localhost", 10022, "127.0.0.1", 22, 10023), // B
      Sub("localhost", 10023, "127.0.0.1", 22, 10024), // C
      Sub("localhost", 10024, "127.0.0.1", 22, 10025), // D
      Sub("localhost", 10025, "127.0.0.1", 22, 10026), // E
      Sub("localhost", 10026, "127.0.0.1", 22, 10027), // F
      Sub("localhost", 10027, "127.0.0.1", 22, 10028), // G
      Sub("localhost", 10028, "127.0.0.1", 22, 10029), // H
      Sub("localhost", 10029, "127.0.0.1", 22, 10030) // I
      )

    def intricate[T](path: Iterable[Sub], curSSHPort: Int = 22)(proc: (SSH) => T): T = {
      path.headOption match {
        case Some(curSub) =>
          SSH.once(curSub.host, defaultUsername, defaultPassword, port = curSub.port) { ssh =>
            ssh.remote2Local(curSub.tport, curSub.fhost, curSub.fport)
            intricate(path.tail, curSub.tport)(proc)
          }
        case None =>
          SSH.once("localhost", defaultUsername, defaultPassword, port = curSSHPort) { ssh =>
            proc(ssh)
          }
      }
    }

    // Build the intricated tunnels and execute a ssh command on the farthest host (I)
    val result = intricate(intricatedPath) { ssh =>
      ssh.executeAndTrim("echo 'Hello intricated world'")
    }

    result should equal("Hello intricated world")
  }

  //==========================================================================================================
  test("remote ssh sessions (ssh tunneling ssh") {
    val rssh = SSH(sshopts).remote(sshopts)
    rssh.options.port should not equals (22)

    rssh.executeAndTrim("echo 'hello'") should equal("hello")

    rssh.close()
  }
  //==========================================================================================================
  test("SCP/SFTP and special system file") {
    SSH.once(sshopts) { ssh =>
      val r = ssh.get("/dev/null")
      r should not equal (None)
      r.get should equal("")
    }
  }

  //==========================================================================================================
  test("sharing SSH options...") {
    val common = (h: String) => SSHOptions(h, username = "test", password = "testtest")

    SSH.once(common("localhost")) { _.executeAndTrim("echo 'hello'") should equal("hello") }

  }

  //==========================================================================================================
  test("env command test") {
    SSH.shell(sshopts) { sh =>
      sh.execute("export ABC=1")
      sh.execute("export XYZ=999")
      val envmap = sh.env
      envmap.keys should (contain("ABC") and contain("XYZ"))
    }
  }

  //==========================================================================================================
  test("exit code tests") {
    SSH.once(sshopts) { ssh =>
      val (_, rc) = ssh.executeWithStatus("(echo toto ; exit 2)")
      rc should equal(2)
    }

    SSH.shell(sshopts) { sh =>
      val (_, rc) = sh.executeWithStatus("(echo toto ; exit 3)")
      rc should equal(3)
    }
  }

  //==========================================================================================================
  test("fred test") {
    SSH.shell(sshopts) { sh =>
      sh.execute("who") should include("test")
    }
  }

}

