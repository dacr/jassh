/*
 * Copyright 2015 David Crosson
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

import com.jcraft.jsch.{JSch, Session}
import java.io.{File,OutputStream}

import language.implicitConversions
import language.reflectiveCalls

/**
 * SSH object factories
 * @author David Crosson
 */
object SSH {

  /**
   * Executes the given code then closes the new ssh associated session.
   * @param host ip address or hostname
   * @param username user name
   * @param password user password (if ommitted, will try public key authentication)
   * @param passphrase keys passphrase (if required)
   * @param port remote ssh port
   * @param timeout timeout
   * @param withssh code bloc to execute
   * @return "withssh" returns type
   */
  def once[T](
    host: String = "localhost",
    username: String = util.Properties.userName,
    password: SSHPassword = NoPassword,
    passphrase: SSHPassword = NoPassword,
    port: Int = 22,
    timeout: Int = 300000)(withssh: (SSH) => T): T = using(new SSH(SSHOptions(host = host, username = username, password = password, passphrase = passphrase, port = port, timeout = timeout))) {
    withssh(_)
  }
  /**
   * Executes the given code then closes the new ssh associated session.
   * @param options ssh options
   * @param withssh code bloc to execute
   * @return "withssh" returns type
   */
  def once[T](options: SSHOptions)(withssh: (SSH) => T): T = using(new SSH(options)) {
    withssh(_)
  }
  /**
   * Executes the given code then closes the new ssh associated session.
   * @param someOptions Some ssh options or None, if None is given, nothing will be done
   * @param withssh code bloc to execute
   * @return "withssh" returns type
   */
  def once[T](someOptions: Option[SSHOptions])(withssh: (SSH) => T): Option[T] = someOptions map { options =>
    using(new SSH(options)) {
      withssh(_)
    }
  }

  /**
   * Executes the given code then closes the new ssh shell channel associated session.
   * @param host ip address or hostname
   * @param username user name
   * @param password user password (if ommitted, will try public key authentication)
   * @param passphrase keys passphrase (if required)
   * @param port remote ssh port
   * @param timeout timeout
   * @param withsh code bloc to execute
   * @return "withsh" returns type
   */
  def shell[T](
    host: String = "localhost",
    username: String = util.Properties.userName,
    password: SSHPassword = NoPassword,
    passphrase: SSHPassword = NoPassword,
    port: Int = 22,
    timeout: Int = 300000)(withsh: (SSHShell) => T): T = shell[T](SSHOptions(host = host, username = username, password = password, passphrase = passphrase, port = port, timeout = timeout))(withsh)

  /**
   * Executes the given code then closes the new ssh shell associated session.
   * @param options ssh options
   * @param withsh code bloc to execute
   * @return "withssh" returns type
   */
  def shell[T](options: SSHOptions)(withsh: (SSHShell) => T): T = using(new SSH(options)) { ssh =>
    ssh.shell { sh => withsh(sh) }
  }
  /**
   * Executes the given code then closes the new ssh shell associated session.
   * @param someOptions Some ssh options or None, if None is given, nothing will be done
   * @param withsh code bloc to execute
   * @return "withssh" returns type
   */
  def shell[T](someOptions: Option[SSHOptions])(withsh: (SSHShell) => T): Option[T] = someOptions map { shell[T](_)(withsh) }

  /**
   * Executes the given code then closes the new ssh powershell channel associated session.
   * @param host ip address or hostname
   * @param username user name
   * @param password user password (if ommitted, will try public key authentication)
   * @param passphrase keys passphrase (if required)
   * @param port remote ssh port
   * @param timeout timeout
   * @param withsh code bloc to execute
   * @return "withsh" returns type
   */
  def powershell[T](
    host: String = "localhost",
    username: String = util.Properties.userName,
    password: SSHPassword = NoPassword,
    passphrase: SSHPassword = NoPassword,
    port: Int = 22,
    timeout: Int = 300000)(withsh: (SSHPowerShell) => T): T = powershell[T](SSHOptions(host = host, username = username, password = password, passphrase = passphrase, port = port, timeout = timeout))(withsh)

  /**
   * Executes the given code then closes the new ssh powershell associated session.
   * @param options ssh options
   * @param withsh code bloc to execute
   * @return "withssh" returns type
   */
  def powershell[T](options: SSHOptions)(withsh: (SSHPowerShell) => T): T = using(new SSH(options)) { ssh =>
    ssh.powershell { sh => withsh(sh) }
  }
  /**
   * Executes the given code then closes the new ssh powershell associated session.
   * @param someOptions Some ssh options or None, if None is given, nothing will be done
   * @param withsh code bloc to execute
   * @return "withssh" returns type
   */
  def powershell[T](someOptions: Option[SSHOptions])(withsh: (SSHPowerShell) => T): Option[T] = someOptions map { powershell[T](_)(withsh) }

  /**
   * Executes the given code then closes the new ssh ftp channel associated session.
   * @param host ip address or hostname
   * @param username user name
   * @param password user password (if ommitted, will try public key authentication)
   * @param passphrase keys passphrase (if required)
   * @param port remote ssh port
   * @param timeout timeout
   * @param withftp code bloc to execute
   * @return "withftp" returns type
   */
  def ftp[T](
    host: String = "localhost",
    username: String = util.Properties.userName,
    password: SSHPassword = NoPassword,
    passphrase: SSHPassword = NoPassword,
    port: Int = 22,
    timeout: Int = 300000)(withftp: (SSHFtp) => T): T = ftp[T](SSHOptions(host = host, username = username, password = password, passphrase = passphrase, port = port, timeout = timeout))(withftp)

  /**
   * Executes the given code then closes the new sftp associated session.
   * @param options ssh options
   * @param withftp code bloc to execute
   * @return "withftp" returns type
   */
  def ftp[T](options: SSHOptions)(withftp: (SSHFtp) => T): T = using(new SSH(options)) { ssh =>
    ssh.ftp { ftp => withftp(ftp) }
  }

  /**
   * Executes the given code then closes the new sftp associated session.
   * @param someOptions Some ssh options or None, if None is given, nothing will be done
   * @param withftp code bloc to execute
   * @return "withftp" returns type
   */
  def ftp[T](someOptions: Option[SSHOptions])(withftp: (SSHFtp) => T): Option[T] = someOptions map { ftp[T](_)(withftp) }

  /**
   * Executes the given code then closes the new ssh shell and ftp channels associated sessions.
   * @param host ip address or hostname
   * @param username user name
   * @param password user password (if ommitted, will try public key authentication)
   * @param passphrase keys passphrase (if required)
   * @param port remote ssh port
   * @param timeout timeout
   * @param withshftp code bloc to execute
   * @return "withshftp" returns type
   */
  def shellAndFtp[T](
    host: String = "localhost",
    username: String = util.Properties.userName,
    password: SSHPassword = NoPassword,
    passphrase: SSHPassword = NoPassword,
    port: Int = 22,
    timeout: Int = 300000)(withshftp: (SSHShell, SSHFtp) => T): T = shellAndFtp[T](SSHOptions(host = host, username = username, password = password, passphrase = passphrase, port = port, timeout = timeout))(withshftp)

  /**
   * Executes the given code then closes the new ssh shell and sftp associated sessions.
   * @param options ssh options
   * @param withshftp code bloc to execute
   * @return "withshftp" returns type
   */
  def shellAndFtp[T](options: SSHOptions)(withshftp: (SSHShell, SSHFtp) => T): T = using(new SSH(options)) { ssh =>
    ssh.shell { sh => ssh.ftp { ftp => withshftp(sh, ftp) } }
  }
  /**
   * Executes the given code then closes the new ssh shell and sftp associated sessions.
   * @param someOptions Some ssh options or None, if None is given, nothing will be done
   * @param withshftp code bloc to execute
   * @return "withshftp" returns type
   */
  def shellAndFtp[T](someOptions: Option[SSHOptions])(withshftp: (SSHShell, SSHFtp) => T): Option[T] = someOptions map { shellAndFtp[T](_)(withshftp) }

  /**
   * Creates a new SSH session, it is up to the user to manage close
   * @param host ip address or hostname
   * @param username user name
   * @param password user password (if ommitted, will try public key authentication)
   * @param passphrase keys passphrase (if required)
   * @param port remote ssh port
   * @param timeout timeout
   * @return SSH session
   */
  def apply(
    host: String = "localhost",
    username: String = util.Properties.userName,
    password: SSHPassword = NoPassword,
    passphrase: SSHPassword = NoPassword,
    port: Int = 22,
    timeout: Int = 300000) = new SSH(SSHOptions(host = host, username = username, password = password, passphrase = passphrase, port = port, timeout = timeout))

  /**
   * Creates a new SSH session, it is up to the user to manage close
   * @param options ssh options
   * @return SSH session
   */
  def apply(options: SSHOptions) = new SSH(options)

  /**
   * Creates a new SSH session, it is up to the user to manage close
   * @param someOptions Some ssh options or None, if None is given, nothing will be done
   * @return Some SSH session or None
   */
  def apply(someOptions: Option[SSHOptions]): Option[SSH] = someOptions map { new SSH(_) }

  protected def using[T <: { def close() }, R](resource: T)(block: T => R) = {
    try block(resource)
    finally resource.close()
  }

}

/**
 * SSH class. This class is the main entry point to the API
 * @author David Crosson
 */
class SSH(val options: SSHOptions) extends ShellOperations with TransfertOperations {
  private implicit val ssh = this
  private val jsch = new JSch
  val jschsession: Session = {
    for {
      ident <- options.identities
      fident = new File(ident.privkey)
      if fident.isFile()
      passphraseOpt = ident.passphrase.password.orElse(options.passphrase.password)
      } {
        passphraseOpt match {
          case Some(pass) => jsch.addIdentity(fident.getAbsolutePath, pass)
          case None => jsch.addIdentity(fident.getAbsolutePath)
        }
    }
      
    val ses = jsch.getSession(options.username, options.host, options.port)
    for {proxy <- options.proxy} ses.setProxy(proxy)
    ses.setServerAliveInterval(2000)
    ses.setTimeout(options.connectTimeout.toInt) // Timeout for the ssh connection (unplug cable to simulate)
    ses.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password")
    ses.setUserInfo(SSHUserInfo(options.password.password, options.passphrase.password))
    ses.connect(options.connectTimeout.toInt)
    if (ssh.options.noneCipher) {
      /* Default : jsch 0.1.48 (2012-06-26)
		  cipher.s2c=aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-cbc,aes256-cbc
		  cipher.c2s=aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-cbc,aes256-cbc
       */
      //ses.setConfig("cipher.s2c", "none,aes128-cbc,3des-cbc,blowfish-cbc")
      //ses.setConfig("cipher.c2s", "none,aes128-cbc,3des-cbc,blowfish-cbc")
      ses.setConfig("cipher.s2c", options.ciphers.mkString(","))
 	    ses.setConfig("cipher.c2s", options.ciphers.mkString(","))
      ses.rekey()
    }
    if (ssh.options.compress.isDefined) {
      ses.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
      ses.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
      ses.setConfig("compression_level", ssh.options.compress.get.toString);
    } else {
      ses.setConfig("compression.s2c", "none,zlib@openssh.com,zlib");
      ses.setConfig("compression.c2s", "none,zlib@openssh.com,zlib");
      ses.setConfig("compression_level", "0");
    }
    ses
  }

  def shell[T](proc: (SSHShell) => T) = SSH.using(new SSHShell) { proc(_) }

  def powershell[T](proc: (SSHPowerShell) => T) = SSH.using(new SSHPowerShell) { proc(_) }

  def ftp[T](proc: (SSHFtp) => T) = SSH.using(new SSHFtp) { proc(_) }

  def scp[T](proc: (SSHScp) => T) = SSH.using(new SSHScp) { proc(_) }

  def noerr(data: ExecResult) {}

  def run(cmd: String, out: ExecResult => Any, err: ExecResult => Any = noerr) = new SSHExec(cmd, out, err)

  override def execute(cmd: SSHCommand) = execOnce(cmd) // Using SSHExec channel (better performances)

  override def executeWithStatus(cmd: SSHCommand): Tuple2[String,Int] = execOnceWithStatus(cmd)

  override def executeAll(cmds: SSHBatch) = shell { _ executeAll cmds }


  def execOnceAndTrim(scmd: SSHCommand) = execOnce(scmd).trim()

  def execOnce(scmd: SSHCommand) = {
    val (result, _) = execOnceWithStatus(scmd)
    result
  }
  def execOnceWithStatus(scmd: SSHCommand): Tuple2[String,Int] = {
    val stdout = new StringBuilder()
    val stderr = new StringBuilder()
    var exitCode = -1
    def outputReceiver(buffer:StringBuilder)(content: ExecResult) {
      content match {
        case ExecPart(part) =>
          if (buffer.size > 0) buffer.append("\n")
          buffer.append(part)
        case ExecEnd(rc) => exitCode=rc.toInt
        case ExecTimeout =>
      }
    }
    var runner: Option[SSHExec] = None
    try {
      runner = Some(new SSHExec(scmd.cmd, outputReceiver(stdout), outputReceiver(stderr)))
      runner foreach { _.waitForEnd }
    } catch {
      case e:InterruptedException =>
        throw new SSHTimeoutException(stdout.toString, stderr.toString)
    } finally {
      runner foreach { _.close }
    }
    (stdout.toString(), exitCode)
  }

  private var firstHasFailed=false

  private def opWithFallback[T]( primary : => T, fallback: => T ):T = {
    if (firstHasFailed) fallback
    else {
      try {
        primary
      } catch {
        case x:RuntimeException if x.getMessage contains "SSH transfert protocol error" =>
          firstHasFailed=true
          fallback
      }
    }
  }

  override def get(remoteFilename: String): Option[String] =
    opWithFallback(
        ssh.scp(_ get remoteFilename),
        ssh.ftp(_ get remoteFilename)
    )


  override def getBytes(remoteFilename: String): Option[Array[Byte]] =
    opWithFallback(
      ssh.scp( _ getBytes remoteFilename),
      ssh.ftp( _ getBytes remoteFilename)
    )

  override def receive(remoteFilename: String, outputStream: OutputStream) {
    opWithFallback(
      ssh.scp(_.receive(remoteFilename, outputStream)),
      ssh.ftp(_.receive(remoteFilename, outputStream))
    )
  }

  override def put(data: String, remoteDestination: String) {
    opWithFallback(
      ssh.scp(_ put (data, remoteDestination)),
      ssh.ftp(_ put (data, remoteDestination))
    )
  }

  override def putBytes(data: Array[Byte], remoteDestination: String) {
    opWithFallback(
      ssh.scp(_ putBytes (data, remoteDestination)),
      ssh.ftp(_ putBytes (data, remoteDestination))
    )
  }

  override def putFromStream(data: java.io.InputStream, howmany:Int, remoteDestination: String) {
    // Never use fallback mechanism for that case, because data stream is consumed...
    ssh.scp(_ putFromStream(data, howmany, remoteDestination))
  }

  override def send(fromLocalFile: File, remoteDestination: String) {
    opWithFallback(
      ssh.scp(_.send(fromLocalFile, remoteDestination)),
      ssh.ftp(_.send(fromLocalFile, remoteDestination))
    )
  }

  override def catData(data:String, filespec:String):Boolean = {
    put(data, filespec)
    true // TODO 
  }
  
  /**
   * Remote host/port => local port (client-side)
   * @param lport remote host port will be mapped on this port on client side (bound to localhost)
   * @param host  remote host (accessed through ssh server side)
   * @param hport remote port (on remote host) to bring back locally
   */
  def remote2Local(lport: Int, host: String, hport: Int) = {
    jschsession.setPortForwardingL(lport, host, hport)
  }

  /**
   * Remote host/port => local port (client-side automatically chosen)
   * @param lport remote host port will be mapped on this port on client side (bound to localhost)
   * @param host  remote host (accessed through ssh server side)
   * @param hport remote port (on remote host) to bring back locally
   * @return chosen local listening port
   */
  def remote2Local(host: String, hport: Int) = {
    jschsession.setPortForwardingL(0, host, hport)
  }

  /**
   * Local (client-side) host/port => Remote host with specified port
   * @param rport the port to create on remote server (where the ssh server stands) to forward lhost/lport
   * @param lhost local host (accessible from ssh client host) from which we'll forward a port
   * @param lport the port to foward
   */
  def local2Remote(rport: Int, lhost: String, lport: Int) {
    jschsession.setPortForwardingR(rport, lhost, lport);
  }

  /**
   * Get access to a remote SSH through current SSH session
   * @param options ssh options
   * @return SSH session
   */
  def remote(remoteOptions: SSHOptions): SSH = {
    val chosenPort: Int = remote2Local(remoteOptions.host, remoteOptions.port)
    val localOptions = remoteOptions.copy(host = "127.0.0.1", port = chosenPort)
    new SSH(localOptions)
  }

  /**
   * Get access to a remote SSH through current SSH session
   * @param host ip address or hostname
   * @param username user name
   * @param password user password (if ommitted, will try public key authentication)
   * @param passphrase keys passphrase (if required)
   * @param port remote ssh port
   * @param timeout timeout
   * @return SSH session
   */
  def remote(
    host: String = "localhost",
    username: String = util.Properties.userName,
    password: SSHPassword = NoPassword,
    passphrase: SSHPassword = NoPassword,
    port: Int = 22,
    timeout: Int = 300000): SSH = remote(SSHOptions(host = host, username = username, password = password, passphrase = passphrase, port = port, timeout = timeout))

  /**
   * returns a new shell for current SSH session, you must manage close operation by your self
   */
  def newShell = new SSHShell

  /**
   * returns a new powershell for current SSH session, you must manage close operation by your self
   */
  def newPowerShell = new SSHPowerShell

  /**
   * returns a new ftp for current SSH session, you must manage close operation by your self
   * @return sftp instance
   */
  def newSftp = new SSHFtp

  /**
   * close current ssh session
   */
  def close() { jschsession.disconnect }

}
