#JASSH - JANALYSE-SSH - SCALA SSH API

Crosson David - crosson.david@gmail.com

##Remarks & caveats:

```
  => ssh persisted shell session operations must be executed within the same thread,
   (this is jassh.jar default behavior as it transparently add the -Yrepl-sync)    
    do not span a persisted shell session across several threads => it may generates exception

     So be careful when using REPL with default config, as each "evaluation" is done within a new thread !
     Workaround : Start the interpreter (REPL) with the "-Yrepl-sync" option.
     
     No problem with SBT as a scala console started from SBT will execute all its entries in the same thread !
     No problem in scala scripts.
     
  => SCP operations can't retrieve special file such as /proc/cpuinfo, because their size are not known !
     Workarounds : use SFTP  OR  use a command such as "cat /proc/cpuinfo".
     (The last one is the "best workaround", will work in all cases)
     
  => Be aware of the fact that SFTP SSH Channel may be not available, 
     so prefer SCP to maximize scripts / code portability from one system to an other.
     Looks like linux SSHD comes with SFTP by default, but not AIX.
     --> Since 0.9.5-b3 , SSH transfert operations comes with an automatic fallback mechanism with priority to scp
         So prefer using SSH transfert operations, over SSHFtp transferts operations.

  => TAKE CARE OF HOW MANY SESSIONS CAN BE MANAGED SIMULTANEOUSLY. Check sshd configuration
     MaxStartups = 10 (default) the maximum number of concurrent unauthenticated connections to the SSH daemon
     MaxSessions = 10 (default) the maximum number of open sessions permitted per network connection

  => ?? With such MaxStartups configuration (at least with OpenSSH_6.9p1-hpn14v5, OpenSSL 1.0.1p 9 Jul 2015) : 
     ?? MaxStartups 10:30:100
     ?? --> Random failures may occurs ?? more frequently ?? than with just "MaxStartups 10" ??
     ??   com.jcraft.jsch.JSchException: session is down
     ??      at com.jcraft.jsch.Channel.sendChannelOpen(Channel.java:762)
     ??   com.jcraft.jsch.JSchException: Session.connect: java.net.SocketException: Connection reset
     ??      at com.jcraft.jsch.Session.connect(Session.java:558)

  => AIX sshd & SSHExecChannel (no persistence) doesn't work well when virtual tty is used
     (execWithPty must be keep to false, this is the default value)
     strange behavior : when SFtp subchannel is enabled, the maximum number of ExecChannel in // decrease...
  
  => Take care of system limits for sshd (nofile & nproc)
     Check /etc/security/limits.conf for linux systems
     if nproc max process/threads limit has been reached for the user you want to connect with, 
       you'll get a failure 

  => Password expiration may ask you for a new password, so you can be blocked waiting for a result that never comes.

  => Remember that some operations may require a TTY (or let's rather say a virtual TTY) or behave 
     differently with or without a TTY/VTTY (sudo, mysql, ...)
     
```

## Major changes

### 0.10.4-SNAPSHOT (2019-06-xx)

- fix integer overflow issue while sending big files / mlahia (https://github.com/mlahia)
- update library dependencies
- update sbt tools (plugins)
- update scala releases

### 0.10.2 (2017-09-29)

- Software updates (scala, scalatest, commons-compress)
- SSHConnectionManager added
  + Take care this is a first implementation
- moved to sbt 1.0 + SBT plugins updates

### 0.10.1 (2017-04-03)

- sbt release integration
- moved to maven central
- scala 2.12 support

### 0.9.20 (2016-06-24)

 - AllOperations trait added : for generic operations that requires both executions and transferts
   + `rreceive` method added to recursively copy a remote directory to a local destination
   + `rsend` method added to recursively copy a local directory to a remote destination
 - SSHShell :
   + `pid` method added
   + `catData` is now scp based !
   + now inherits from SSHScp and is able to perform file transfert operations
   + now comes with AllOperations trait instead of ShellOperations
 - SSH :
   + now comes with AllOperations trait instead of both ShellOperations and TransfertOperations
 - ShellOperations :
   + `which` method added
 - jsch rekey operation disabled => it generates random "session is down" ssh error !
   the same for ciphers...
 - dependencies update :
   + sbt assembly 0.14.1
   + commons-compress 1.11
   + sl4j-api 1.7.21
   + scalatest 2.2.6
   + scala 2.10.6 & 2.11.8
   + jsch 0.1.54


### 0.9.19 (2015-09-22)

 - ShellOperations : sudoNoPasswordTest renamed to sudoSuMinusOnlyWithoutPasswordTest
 - ShellOperations : sudo operations moved to SSHShell class
 - SSHShell : catData method added
 - SSHShell : sudoSuMinusOnlyWithPasswordTest method added
 - SSHShell : sudoSuMinusWithCommandTest method added
 - SSHShell : executeWithExpects quick'n dirty implementation
 - SSHOptions :
   + sshUserDir sshKeyFile parameters removed.
   + replaced by identities parameter which is prefilled 
     with found identifies in $HOME/.ssh/ such as id_rsa, id_dsa, id_ecdsa, identity, ...
   + SSHOptions.addIdentity can be used to add a new identity, new ones are added first
   + SSHOptions(identities=SSHIdentity(...)::Nil) can be use to specify a particular identity
 - Merged : Added cd, rm, and rmdir to sftp coming from : mgregson (https://github.com/mgregson)
 - scala 2.11.7
 - jsch 0.1.53
 - ses.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password")
   added as suggested by herbinator (https://github.com/herbinator)

### 0.9.18 (2015-03-22)

 - jsch 0.1.52 
 - scalalogging usage removed because of api incompatibilities between scala 2.10 and 2.11 !!
   and because no support for scala2.10 in scala-logging 3.x
   replaced by slf4j-api
 - junit test dependency removed
 - onejar subproject, sbt assembly release update (0.13)

### 0.9.17 (2015-03-22)

 - ShellOperations : mkcd added (mkdir && cd tied together)
 - ShellOperations : mkdir now returns true if successfull
 - ShellOperations : rmdir now returns true if successfull
 - more tests for ShellOperations, increased coverage => with various related fixes
 - Depend on scalalogging instead of scalalogging-slf4j : mgregson (https://github.com/mgregson)
 
### 0.9.16 (2015-03-18)

 - ShellOperations : echo added
 - ShellOperations : alive added
 - become tests ignored

### 0.9.15 (2015-03-18)

 - SSHShell : become enhancements (su - or sudo su - support)
 - ShellOperations : sudoNoPasswordTest added
 - ShellOperations : dirname added
 - ShellOperations : basename added
 - ShellOperations : lastModified fix because of millis
 - ShellOperations : id added
 - ShellOperations : touch added
 - SSH through tunnel support added (ProxyHTTP, ProxySocks4, ProxySocks5)
 - scala 2.11.6
 - commons-compress 1.9
 - scala-test 2.2.1
 - sudoNoPasswordTest test enhancement for older sudo command releases (without -n option)


### 0.9.14 (2014-09-23)

 - small test fix on test shell command
 - pull request merged : add Travis CI configuration file from zaneli authored on 29 Apr
 - scala 2.11.2
 - commons-compress 1.8.1
 - ShellOperations : lastModified darwin implementation added + test case
 - ShellOperations : disableHistory method added + test case
 - ShellOperationsTest class added.
 - small tests cleanup and improvements
 - shell history can now be processed
 - SSHReact class first implementation, it allows you to interact with a running command
   (for example send the password at the right moment, or enter values required by a read command)
 - SSHReact test class added
 - new shell commands :
   + pidof : get pid of all processes matching the given command line regular expression
   + disableHistory : to not impact current user shell commands history
   + uptime : get the uptime of the server

### 0.9.13 (2014-05-31)

 - scala 2.11 support added
 - scala-logging-slf4j 2.1.2
 - scala-io 0.4.3
 - scalatest 2.1.5
 - jsch 0.1.51
 - and various impact changes

### 0.9.12 (2014-01-20)

 - sbt-eclipse 2.4.0
 - sbt-assembly 0.10.2
 - scalatest 2.0
 - scala 2.10.3
 - commons-compress 1.7

### 0.9.11 (2013-09-24)

 - receiveNcompress was not using localbasename parameter with already compressed remote files.
 - sbt-eclipse 2.3.0
 - fix from "Shashank Jain" that extends the cipher list.
   none,aes128-cbc,aes192-cbc,aes256-cbc,3des-cbc,blowfish-cbc,aes128-ctr,aes192-ctr,aes256-ctr
 - ciphers parameter added to SSHOptions in order to allow custom ciphers list
   
### 0.9.10 (2013-07-03)

 - DONE : add su support / with password prompt recognition...
    SSHShell.become method added, allow to become someoneelse.
    typical usage : from an ordinary user become the root user
 - SomeHelp class added in tests, in order to share common stuff.
 - various test classes enhancements
 - SSHShell timeout on current command (send ^C to break current processing and give back the prompt)
 - exit code accessible through a new method named executeWithStatus (thanks Alex Biehl for the suggestion)
 - SSHAPI.scala file split finished, SSHAPI.scala file deleted
 - imports cleaned up, source file copyright & license header added
 - fix : ls on an empty directory was returning a collection containing an empty string
 - fix : SSHFtp.receive was not flushing...  receiveNcompress was broken with SSHFtp, now it is OK.
 - fix : break on timeout may block. resultsQueue.poll with timeout instead of a forever resultsQueue.take
 - new method in TransfertOperations : putFromStream(data: java.io.InputStream, howmany:Int, remoteDestination: String)
 - jsch 0.1.50
 - sbteclipse-plugin 2.2.0
 - scala 2.10.2
 - sbt assembly 0.9.0
 
### 0.9.9 (2013-05-09)

 - scala 2.10.1
 - scalalogging 1.0.1
 - sbt assembly 0.8.8
 - sbt eclipse 2.1.2
 - removing parenthesis to cd to allow executing "cd" to go back to home directory
 - SSHExec timeout implemented BUT not yet clean
    + SSHTimeoutException class added
 - receiveNcompress method added to TransfertOperations (gz)
 - new dependency : "org.apache.commons" % "commons-compress"
 - SSHOptions - removing second parameter list, host comes back to the first and unik parameter list.
   (Two parameters list for SSHOptions was a bad idea)

### 0.9.8 (2013-02-22)

 - jsch session is now configured with tcp keep alive of 2s
   (setServerAliveInterval(2000))
 - AIX sha1sum & md5sum fix.
 - new commands :
   + env
   + osid  (with new trait OS and objects AIX, Linux, Darwin, SunOS)
   + rm
   + rmdir
   + mkdir
   + arch
   + kill
 - new test cases 
 - "du" fix for AIX (not supporting -d or --max-depth option)
 - "ps" fix for AIX
 - SSHOptions new option : execWithPty = false (by default)
   tell if exec should use a virtual tty or not
   (Feature : ChannelExec use of virtual tty must be configurable, as without performance are quite better)
 - onejar subproject contains now a simplified launcher
   with default scala options :
   -Yrepl-sync -usejavacp
   -nocompdaemon -savecompiled
   -deprecation 

### 0.9.7 (2013-02-10)

 - scala >=2.10 is now mandatory
 - ChannelExec now is using a virtual tty by default
 - new commands : 
   + fsFreeSpace
   + fileRights
   + du
 - minor updates for darwin (mac os x) support
 - ps() command enhancements 
   + OS process modeling => LinuxProcess, AIXProcess, SunOSProcess, DarwinProcess
   + LinuxProcessState, DarwinProcessState
   + ps test case added
 - logging support added (using scala-logging)

### 0.9.6 (2013-01-07)

 - scala 2.10.0 support added

### 0.9.5-b3 (2012-11-26)

 - general transfert methods (available in SSH class) are now using automatic fallback, if SCP fails, then SFTP will be tryied.

### 0.9.5-b2 (2012-11-05)

 - JSCH updated to 0.1.49
 - now using sbt 0.12.1
 - now using scalatest 2.0-M5
 - now using sbteclipse 2.1.0
 - now using sbt assembly 0.8.5
 - add support for scala 2.10.0-RC2
 - rexec.scala example script added 
 - Issue 1 Fixed: ssh keys reported by jendap
   SSHOptions new parameter : sshKeyFile: Option[String]=None, // if None, will look for default names. (sshUserDir is used)
 - new helper methods : 
   => ps : to get the list of running processes
   => cat file : to get the content of a file through the cat command.
      Useful when trying to get special linux file content 
 - remote2Local(host:String, hport:Int) without local port specified; 
   the port is automatically chosen and returned
 - get access to a remote SSH through current SSH session
   SSH.remote(options:SSHOptions):SSH
   SSH.remote(remotePort:Int, options:SSHOptions)
 - SSHOptions API CHANGE, a second parameter list have been added, host parameter moved from first one to the second one
   GOAL : Allow simple creation of partial function with SSHOptions, to share ssh options between several connections...
 - TransferOperations new methods :
   send(filename: String)
   receive(filename: String)
 - ShellOpterations new methods : 
   notExists(filename: String): Boolean
 - CommonOperations trait added, inherited by both TransfertOperations & ShellOperations
   define the following method :
   localmd5sum(filename:String):Option[String]
   
### 0.9.3

 - now using sbt-assembly 0.8.3
 - fixes relatives to implicit conversions with SSHPassword
 - fixes relatives to implicit conversion to SSHCommand and SSHBatch
 - For SSHBatch : execute, executeAndTrim, executeAndTrimSplit
     renamed to : executeAll, executeAllAndTrim, executeAllAndTrimSplit
 - Using Iterable instead of List
 - external (package) usage tests completed (ExternalSSHAPITest.scala)
 - small fix about how private key passphrase is taken into account (when pub-key auth is used)

### 0.9.2

 - date '+%Y-%m-%d %H:%M:%S %z' %z and %Z gives the same result on AIX, this result corresponds to linux %Z
   So modifying code to use %Z instead of %z.
   Now using GMT, "date -u '+%Y-%m-%d %H:%M:%S %Z'" in order to everything work well in all cases.
 - SSH.once(Option[SSHOptions]) fix linked to Option type result not at the right place
 - New test source file : ExternalSSHAPITest.scala => Testing the API from an external package 
 - Fixed : minor problem with script when invoking jajmx.SSH... or fr.janalyse.sh.SSH... without imports...
 
### 0.9.1

 - SSH tunneling fix, cleanup, and scaladocumented
 - Intricated SSH tunneling test added (self intrication, to simplify test case)
 
### 0.9.0

 - now using sbt-assembly 0.8.1
 - now using scalatest 0.8
 - SSHCommand, SSHBatch methods ! renamed
 - new helper methods :
   test, exists, isFile, isDirectory, isExecutable 
 - findAfterDate & date helper fix !!
   Shell.date -> remote system time zone is now taken into account
 - Test cases fixes :
   Forcing parallelism to 6 ! for test case "Simultaenous SSH operations"
 - Code factorization :
   => ShellOperations trait added. Inherited by SSH and SSHShell.
   => TransferOperations trait added. Inherited by SSH and SSHFtp
 - SCP supported, for no-persistent transferts sessions, SCP is now used by default (instead of SFTP)
   (e.g. : SSH class transfert operation is now using SCP by default).
 - noneCipher switch added to SSHOptions for higher performance SCP transfert (true by default)
   (http://www.psc.edu/index.php/hpn-ssh)
 - transfert (receive) tests added
   Reference time on a local system: 500Mb using 5 SCP command (100Mb/cmd) takes on the same system 8.7s  (~62Mo/s by file)
   [info] - file transfert performances (with content loaded in memory)
   [info]   + Bytes rate : 38,6Mb/s 500Mb in 12,9s for 5 files - byterates using SCP 
   [info]   + Bytes rate : 44,9Mb/s 500Mb in 11,1s for 5 files - byterates using SCP (with none cipher) 
   [info]   + Bytes rate : 38,5Mb/s 500Mb in 13,0s for 5 files - byterates using SFTP 
   [info]   + Bytes rate : 46,0Mb/s 500Mb in 10,9s for 5 files - byterates using SFTP (with none cipher) 
   [info]   + Bytes rate : 39,5Mb/s 500Mb in 12,7s for 5 files - byterates using SFTP (session reused 
   [info]   + Bytes rate : 46,7Mb/s 500Mb in 10,7s for 5 files - byterates using SFTP (session reused, with none cipher) 
   [info]   + Bytes rate : 29,5Mb/s 500Mb in 16,9s for 500 files - byterates using SCP 
   [info]   + Bytes rate : 32,1Mb/s 500Mb in 15,6s for 500 files - byterates using SCP (with none cipher) 
   [info]   + Bytes rate : 26,7Mb/s 500Mb in 18,7s for 500 files - byterates using SFTP 
   [info]   + Bytes rate : 29,5Mb/s 500Mb in 16,9s for 500 files - byterates using SFTP (with none cipher) 
   [info]   + Bytes rate : 37,7Mb/s 500Mb in 13,3s for 500 files - byterates using SFTP (session reused) 
   [info]   + Bytes rate : 43,7Mb/s 500Mb in 11,4s for 500 files - byterates using SFTP (session reused, with none cipher) 
 - Code cleanup & Scaladocumenting
 - SSH compression now supported
 - For easier SSH Tunneling, new methods are now available :
   + def remote2Local(rport:Int, lhost:String, lport:Int)
   + def local2Remote(lport:Int, rhost:String, rport:Int) 
  
### 0.8.0

 - now using sbt 0.11.3
 - now using sbteclipse 2.1.0-RC1
 - Set of new method to help with commons remote commands :
   fileSize, md5sum, sha1sum, uname, ls, pwd, cd(*), hostname, date, findAfterDate
   (*) of course only for shell sessions 
 - JSCH updated to 0.1.48
 - md5sum method added to SSHTools object
 - manage well connect timeout (default = 30s) and general socket timeout (default = 5mn)

### 0.7.4 

 - SSHPassword toString method added (return the password)
 - updated for scala 2.9.2 support
 - scalatest 1.7.2
 - no more support for scala 2.8.1 & 2.8.2


### 0.7.3

 - JCSH updated to release 0.1.47
 - SSHOptions now contains an extra field "name" which allow user to friendly identify a remote ssh system
 - SSHOptions password type is now of SSHPassword type instead of String.
   Implicit conversions is provided from String, Option[String]
 - SSHShell batch method renamed to execute


### 0.7.2

 - added a package object jassh to define shortcuts to fr.janalyse.ssh.SSH class and object
 - SSHOptions, host parameter is now in first position !


### 0.7.1

 - fix big issue with SSHShell results separator process. => not seen using localhost tests => Must add remote tests !!


### 0.7.0

 - Added new method to SSH : newShell & newSftp for user to manage themselves shell and sftp session
 - Some internal changes to SSHExec class, in order to try to remove actor dependency. Mixing actors systems looks problematic
 - SSHShell new implementation, no more actors used, better performances and behavior, ... (throughput : 504 cmd/s using persistency)
 - SSHExec last result line is no longer lost
 - SSHOptions : new parameter : "prompt" to enable custom shell or console command to be use.
                prompt provide to SSHShell the way to separate command results
 - SSHOptions : connectionTimeout renamed into timeout
 - Various cleanup and enhancements
 - Tests : compare performances persistent SSHShell versus SSHExec  commands throughputs
 - SSH : Add an execute immediate method which rely on SSHExec, not SSHShell ! (throughput : 62cmd/s) 
         execOnce & execOnceAndTrim
 - SSHExec : Do not rely on DaemonActor/Actor anymore
 - SSHShell : Removed init Thread.sleep => Better performances (throughput : 37 cmd/s instead 1cmd/s)
 - SSH.connect becomes SSH.once
 - Removing apply in SSH class as it may encourage bad usage, and close not called

### 0.6.0

 - update jsch to 0.1.46
 - update sbteclipse plugin to 2.0.0
 - update sbtassembly plugin to 0.7.3
 - background ssh execution API changes (run method)
 - temporary hack to remove CPU overhead within run method

