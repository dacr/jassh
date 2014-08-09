package fr.janalyse.ssh

import java.io._
import com.jcraft.jsch.{ ChannelShell }
import java.util.concurrent.ArrayBlockingQueue
import scala.concurrent._



class SSHReact(implicit ssh: SSH) extends ShellOperations {
  // ------------------------------------------------
  val options: fr.janalyse.ssh.SSHOptions = ssh.options
  // ------------------------------------------------  
  def execute(that: fr.janalyse.ssh.SSHCommand): String = ???
  def executeWithStatus(that: fr.janalyse.ssh.SSHCommand): (String, Int) = ???

  // ------------------------------------------------
  def perform(that: SSHCommand)(implicit ctx: ExecutionContext): Future[String] = future {
    reactor.send(that.cmd)
    ""
  }

  // ------------------------------------------------
  /*

SSHReact

OutputStream => were to write (executed by current thread)
CustomOutputStream => which receives data (executed an internal jsch thread)

   */
  // ------------------------------------------------

  val (channel, reactor) = {
    var ch: ChannelShell = ssh.jschsession.openChannel("shell").asInstanceOf[ChannelShell]
    ch.setPtyType("dumb")
    ch.setXForwarding(false)
    //ch.setEnv("COLUMNS", "500") // Can't be use, by default PermitUserEnvironment=no in sshd_config 

    val pos = new PipedOutputStream()
    val pis = new PipedInputStream(pos)
    val reactor = new Reactor(pos)

    ch.setInputStream(pis)
    ch.setOutputStream(reactor)

    ch.connect(ssh.options.connectTimeout.toInt)

    (ch, reactor)
  }

  def close() = {
    reactor.close()
    channel.disconnect()
  }

  class Reactor(output: OutputStream) extends OutputStream {
    private def sendChar(char: Int) {
      output.write(char)
      output.flush()
    }
    private def sendString(cmd: String) {
      output.write(cmd.getBytes)
      nl()
      output.flush()
    }
    def send(cmd: String) { sendString(cmd) }

    def break() { sendChar(3) } // Ctrl-C
    def exit() { sendChar(4) } // Ctrl-D
    def excape() { sendChar(27) } // ESC
    def nl() { sendChar(10) } // LF or NEWLINE or ENTER or Ctrl-J
    def cr() { sendChar(13) } // CR

    override def close() {
      output.close()
      super.close()
    }

    // ----------
    def write(b: Int) {
    }
  }

}
