package fr.janalyse.ssh

import java.io._
import com.jcraft.jsch.{ ChannelShell }
import java.util.concurrent.ArrayBlockingQueue

class SSHShell(implicit ssh: SSH) extends ShellOperations {

  
  /**
   * Does the command "sudo su -" without password works ?
   * The typical usage that maximizes compatibilities accross various linux is to pipe the
   *    command to the sudo -S su -
   * Options such as -k, -A, -p ... may not be supported everywhere. 
   * Some notes :
   *     BAD because we want to test the su
   *       sudo -n echo OK 2>/dev/null
   *
   *     BAD because with older linux, -n option was not available
   *       sudo -n su - -c "echo OK" 2>/dev/null
   *
   *     ~GOOD but NOK if only su - is allowed 
   *       echo | sudo -S su - -c echo "OK" 2>/dev/null
   *
   *     GOOD
   *       echo "echo OK" | sudo -S su - 2>/dev/null
   * @return true if just "sudo su -" is possible without password for current user
   */
  def sudoSuMinusOnlyWithoutPasswordTest():Boolean = {
     val testedmsg="SUDOOK"
     execute(s"""echo "echo $testedmsg" | sudo -S su - 2>/dev/null""").trim.contains(testedmsg)
  }

  /**
   * Does the sudo su - command works with the current user password ?
   *   while preserving the TTY stdin !
   * @return true if OK
   */
  def sudoSuMinusOnlyWithPasswordTest():Boolean = {
    val cur = s"""SUDO_PROMPT="password:" sudo -S su -"""
    ???
  }
  
  /**
   * Does the command sudo "su - -c theGivenCommand" works ?
   * Transparently with or without password
   * @return true if it works
   */
  def sudoSuMinusWithCommandTest(cmd:String="whoami"):Boolean = {
    val password=options.password.password.getOrElse("")
    val scriptname=".custom-askpass-"+(scala.math.random*10000000l).toLong
    val script=
     s"""
        |echo '$password'
        |#self destruction
        |rm -f $$HOME/$scriptname
        |""".stripMargin
    catData(script, s"""$$HOME/$scriptname""")
    execute(s"""chmod u+x $$HOME/$scriptname""")
    execute(s"""$$HOME/$scriptname | SUDO_PROMPT="" sudo -S su - -c "$cmd" >/dev/null 2>&1 ; echo $$?""")
      .trim
      .equals("0")
  }

  
  /**
   * write some data to the specified filespec
   * @param filespec the file to write to
   * @return true if data was written to the given file destination
   */
  override def catData(data:String, filespec:String):Boolean = {
    synchronized {
      execute(s"""touch "$filespec" >/dev/null 2>&1 ; echo $$?""").trim().equals("0") match {
        case false => false
        case true => 
          sendCommand(s"""cat > "$filespec" """)
          toServer.write(data)
          toServer.nl()
          toServer.eot()
          val ignored = fromServer.getResponse()
          true
      }
    }
  }

  
  override def execute(cmd: SSHCommand): String = {
    synchronized {
      sendCommand(cmd.cmd)
      fromServer.getResponse()
    }
  }

  override def executeWithStatus(cmd: SSHCommand): Tuple2[String, Int] = {
    synchronized {
      val result = execute(cmd)
      val rc = executeAndTrim("echo $?").toInt
      (result, rc)
    }
  }

  private def becomeWithSU(someoneelse: String, password: Option[String] = None):Boolean = {
    val curuser = whoami
    if (curuser == "root") {
      execute("LANG=en; export LANG")
      sendCommand(s"su - ${someoneelse}")
      Thread.sleep(2000) // TODO - TO BE IMPROVED
      shellInit()
    } else if (password.isDefined) {
      execute("LANG=en; export LANG")
      sendCommand(s"su - ${someoneelse}")
      Thread.sleep(2000) // TODO - TO BE IMPROVED
      try {
          password.foreach { it => toServer.send(it) }
          Thread.sleep(1000)
      } finally {
        shellInit()
      }
    }
      whoami == someoneelse
  }
  private def becomeWithSUDO(someoneelse: String):Boolean = {
    val curuser = whoami
    if (sudoSuMinusOnlyWithoutPasswordTest()) {
      execute("LANG=en; export LANG")
      sendCommand(s"sudo -n su - ${someoneelse}")
      shellInit()
    } else {
      execute("LANG=en; export LANG")
      sendCommand(s"sudo -S su - ${someoneelse}")
      Thread.sleep(2000)  // TODO - TO BE IMPROVED
      try {
        if (curuser != "root") { // do not use whoami here as we are in transitional state...
          options.password.password.foreach{ it => toServer.send(it)}
          Thread.sleep(1000)
        }
      } finally {
        shellInit()
      }
    }
    whoami == someoneelse
  }  
  /**
   * Become someoneelse on the current shell session, first the command
   * will try (if new user password is given) su - newuser then if unsuccessful
   * it will try the sudo su - approach, in that case it is the current user
   * pass that will be used, new user password will be ignored.
   *  
   * @param someoneelse become this new user
   * @param password new user password
   * @return true if operation is successfull, the current user is the new one
   */
  def become(someoneelse: String, password: Option[String] = None): Boolean = {
    if (whoami != someoneelse) {
      synchronized {
        becomeWithSU(someoneelse, password) ||
          becomeWithSUDO(someoneelse)
      }
    } else true
  }

  private def createReadyMessage = "ready-" + System.currentTimeMillis()
  private val defaultPrompt = """_T-:+"""
  private val customPromptGiven = ssh.options.prompt.isDefined
  val prompt = ssh.options.prompt getOrElse defaultPrompt

  val options = ssh.options

  private val (channel, toServer, fromServer) = {
    var ch: ChannelShell = ssh.jschsession.openChannel("shell").asInstanceOf[ChannelShell]
    ch.setPtyType("dumb")
    ch.setXForwarding(false)
    //ch.setEnv("COLUMNS", "500") // Can't be use, by default PermitUserEnvironment=no in sshd_config 

    val pos = new PipedOutputStream()
    val pis = new PipedInputStream(pos)
    val toServer = new Producer(pos)
    ch.setInputStream(pis)

    val fromServer = new ConsumerOutputStream(customPromptGiven) // if the customPrompt is given, we consider we're ready to send/receive commands
    ch.setOutputStream(fromServer)

    ch.connect(ssh.options.connectTimeout.toInt)

    (ch, toServer, fromServer)
  }

  def close() = {
    fromServer.close()
    toServer.close()
    channel.disconnect()
  }

  private def shellInit() = {
    if (ssh.options.prompt.isEmpty) {
      // if no prompt is given we assume that a standard sh/bash/ksh shell is used
      val readyMessage = createReadyMessage
      fromServer.setReadyMessage(readyMessage)
      toServer.send("unset LS_COLORS")
      toServer.send("unset EDITOR")
      toServer.send("unset PAGER")
      toServer.send("COLUMNS=500")
      toServer.send("PS1='%s'".format(defaultPrompt))
      toServer.send("history -d $((HISTCMD-2)) && history -d $((HISTCMD-1))") // Previous command must be hidden
      //toServer.sendCommand("set +o emacs")  // => Makes everything not working anymore, JSCH problem ?
      //toServer.sendCommand("set +o vi") // => Makes everything not working anymore, JSCH problem ?
      toServer.send("echo '%s'".format(readyMessage)) // ' are important to distinguish between the command and the result
      fromServer.waitReady()
      fromServer.getResponse() // ready response
    } else {
      fromServer.waitReady()
      fromServer.getResponse() // For the initial prompt
    }
  }

  private var doInit = true
  private def sendCommand(cmd: String): Unit = {
    if (doInit) {
      shellInit()
      doInit = false
    }
    toServer.send(cmd)
  }

  
  // -----------------------------------------------------------------------------------
  class Producer(output: OutputStream) {
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
    def write(str: String) {
      output.write(str.getBytes)
      output.flush()
    }

    def brk() { sendChar(3) } // Ctrl-C
    def eot() { sendChar(4) } // Ctrl-D - End of Transmission
    def esc() { sendChar(27) } // ESC
    def nl() { sendChar(10) } // LF or NEWLINE or ENTER or Ctrl-J
    def cr() { sendChar(13) } // CR


    def close() { output.close() }
  }

  // -----------------------------------------------------------------------------------
  class ConsumerOutputStream(checkReady: Boolean) extends OutputStream {
    import java.util.concurrent.TimeUnit

    private val resultsQueue = new ArrayBlockingQueue[String](10)

    def hasResponse() = resultsQueue.size > 0

    def getResponse(timeout: Long = ssh.options.timeout) = {
      if (timeout == 0L) resultsQueue.take()
      else {
        resultsQueue.poll(timeout, TimeUnit.MILLISECONDS) match {
          case null =>
            toServer.brk()
            //val output = resultsQueue.take() => Already be blocked with this wait instruction...
            val output = resultsQueue.poll(5, TimeUnit.SECONDS) match {
              case null => "**no return value - couldn't break current operation**"
              case x => x
            }
            throw new SSHTimeoutException(output, "") // We couldn't distinguish stdout from stderr within a shell session
          case x => x
        }
      }
    }

    def setReadyMessage(newReadyMessage: String) = {
      ready = checkReady
      readyMessage = newReadyMessage
      readyMessageQuotePrefix = "'" + newReadyMessage
    }
    private var readyMessage = ""
    private var ready = checkReady
    private val readyQueue = new ArrayBlockingQueue[String](1)
    def waitReady() {
      if (ready == false) readyQueue.take()
    }
    private var readyMessageQuotePrefix = "'" + readyMessage
    private val promptEqualPrefix = "=" + prompt

    private val consumerAppender = new StringBuilder(8192)
    private val promptSize = prompt.size
    private val lastPromptChars = prompt.reverse.take(2).reverse
    private var searchForPromptIndex = 0

    def write(b: Int) {
      if (b != 13) { //CR removed... CR is always added by JSCH !!!!
        val ch = b.toChar
        consumerAppender.append(ch) // TODO - Add charset support
        if (!ready) { // We want the response and only the response, not the echoed command, that's why the quote is prefixed
          if (consumerAppender.endsWith(readyMessage) &&
            !consumerAppender.endsWith(readyMessageQuotePrefix)) {
            // wait for at least some results, will tell us that the ssh cnx is ready
            ready = true
            readyQueue.put("ready")
          }
        } else if (consumerAppender.endsWith(lastPromptChars)
          && consumerAppender.endsWith(prompt)
          && !consumerAppender.endsWith(promptEqualPrefix)) {
          val promptIndex = consumerAppender.size - promptSize
          val firstNlIndex = consumerAppender.indexOf("\n")
          val result = consumerAppender.substring(firstNlIndex + 1, promptIndex)
          resultsQueue.put(result)
          searchForPromptIndex = 0
          consumerAppender.clear
        } else {
          searchForPromptIndex = consumerAppender.size - promptSize
          if (searchForPromptIndex < 0) searchForPromptIndex = 0
        }
      }
    }
  }

}