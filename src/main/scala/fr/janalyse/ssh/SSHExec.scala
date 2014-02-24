package fr.janalyse.ssh

import com.jcraft.jsch.{ChannelExec}
import java.nio.charset.Charset
import java.nio.ByteBuffer
import java.io.{InputStream, BufferedInputStream, InterruptedIOException}

class SSHExec(cmd: String, out: ExecResult => Any, err: ExecResult => Any)(implicit ssh: SSH) {

  private val (channel, stdout, stderr, stdin) = {
    val ch = ssh.jschsession.openChannel("exec").asInstanceOf[ChannelExec]
    ch.setCommand(cmd.getBytes())
    val stdout = ch.getInputStream()
    val stderr = ch.getErrStream()
    val stdin = ch.getOutputStream()
    ch.setPty(ssh.options.execWithPty)
    ch.connect(ssh.options.connectTimeout.toInt)
    (ch, stdout, stderr, stdin)
  }
  private val stdoutThread = InputStreamThread(channel, stdout, out)
  private val stderrThread = InputStreamThread(channel, stderr, err)
  private val timeoutThread = TimeoutManagerThread(ssh.options.timeout) {
    stdoutThread.interrupt()
    stderrThread.interrupt()
    }

  def giveInputLine(line: String) {
    stdin.write(line.getBytes())
    stdin.write("\n".getBytes())
    stdin.flush()
  }

  def waitForEnd {
    stdoutThread.join()
    stderrThread.join()
    if (timeoutThread.interrupted) throw new InterruptedException("Timeout Reached")
    close()
  }

  def close() {
    stdin.close()
    stdoutThread.interrupt()
    stderrThread.interrupt()
    channel.disconnect
    timeoutThread.interrupt()
  }

  private class TimeoutManagerThread(timeout:Long)(todo : =>Any) extends Thread {
    var interrupted=false
    override def run() {
      if (timeout>0) {
	      try {
	        Thread.sleep(timeout)
	        interrupted=true
	        todo
	      } catch {
	        case e:InterruptedException => 
	      }
      }
    }
  }
  private object TimeoutManagerThread {
    def apply(timeout:Long)(todo : => Any):TimeoutManagerThread = {
      val thread = new TimeoutManagerThread(timeout)(todo)
      thread.start()
      thread
    }
  }
  
  private class InputStreamThread(channel: ChannelExec, input: InputStream, output: ExecResult => Any) extends Thread {
    override def run() {
      val bufsize = 16 * 1024
      val charset = Charset.forName(ssh.options.charset)
      val binput = new BufferedInputStream(input)
      val bytes = Array.ofDim[Byte](bufsize)
      val buffer = ByteBuffer.allocate(bufsize)
      val appender = new StringBuilder()
      var eofreached = false
      try {
	      do {
	        // Notes : It is important to try to read something even available == 0 in order to be able to get EOF message !
	        // Notes : After some tests, looks like jsch input stream is probably line oriented... so no need to use available !
	        val howmany = binput.read(bytes, 0, bufsize /*if (available < bufsize) available else bufsize*/ )
	        if (howmany == -1) eofreached = true
	        if (howmany > 0) {
	          buffer.put(bytes, 0, howmany)
	          buffer.flip()
	          val cbOut = charset.decode(buffer)
	          buffer.compact()
	          appender.append(cbOut.toString())
	          var s = 0
	          var e = 0
	          do {
	            e = appender.indexOf("\n", s)
	            if (e >= 0) {
	              output(ExecPart(appender.substring(s, e)))
	              s = e + 1
	            }
	          } while (e != -1)
	          appender.delete(0, s)
	        }
	      } while (!eofreached) // && !channel.isEOF() && !channel.isClosed()) // => This old test is not good as data may remaining on the stream
          if (appender.size > 0) output(ExecPart(appender.toString()))
	      output(ExecEnd(channel.getExitStatus()))
      } catch {
        case e:InterruptedIOException =>
          output(ExecTimeout)
        case e:InterruptedException =>
          output(ExecTimeout)
      }
    }
  }
  private object InputStreamThread {
    def apply(channel: ChannelExec, input: InputStream, output: ExecResult => Any) = {
      val newthread = new InputStreamThread(channel, input, output)
      newthread.start()
      newthread
    }
  }

}
