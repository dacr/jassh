#JASSH - SCALA SSH API#

[![Build Status](https://travis-ci.org/dacr/jassh.png?branch=master)](https://travis-ci.org/dacr/jassh)

High level scala SSH API for easy and fast operations on remote servers.

This API is [JSCH](http://www.jcraft.com/jsch/) based. Interfaces are stable. Many helper functions are provided to simplify unix operations [ps, ls, cat, kill, find, ...](http://www.janalyse.fr/scaladocs/janalyse-ssh/#fr.janalyse.ssh.ShellOperations), an other goal of this API is to create an unix abstraction layer (Linux, Aix, Solaris, Darwin, ...).

One of the main difference of this API with others is that it can work with **persisted shell sessions**. Many commands can then be sent
to an already running and **initialized** shell session ! Thanks to this feature you can greatly speed up your SSH shell performances,
from 70 cmd/s to more than 500 hit/s ! There is no differences in API between persisted and not persisted shell sessions, that's the
reason why the API looks very simple from scala point of view; when you execute a command in a shell persisted session you get directly
the output of the command but not the return code. The return code will be accessible only indirectly using for example a "echo $?" command.

The current release doesn't provide full shell interaction with executed commands, you only send a command and get the result, but
I'm currently working to provide full interactivity, to allow interaction with commands such as to provide data after the command is
started (send a password once the prompt is visible, ...). This work is currently visible through SSHReact class and SSHReactTest
test class.  

[**JAnalyse software maven repository**](http://www.janalyse.fr/repository/)

[**Scala docs**](http://www.janalyse.fr/scaladocs/janalyse-ssh)

**Current releases** :  **0.9.19** (for scala 2.10 and 2.11)   **0.9.5b3** (for scala 2.9.1, 2.9.2)

**Declare dependency in SBT as follow** :
```
libraryDependencies += "fr.janalyse"   %% "janalyse-ssh" % "0.9.19" % "compile"
```

**Add JAnalyse repository in SBT as follow** :
```
resolvers += "JAnalyse Repository" at "http://www.janalyse.fr/repository/"
```

**The standalone jassh executable** : [jassh.jar](http://dnld.crosson.org/jassh.jar)

The procedure to generate your own executable :
```bash
$ git clone https://github.com/dacr/jassh.git
$ cd jassh/
$ sbt assembly
...
[info] Packaging target/scala-2.11/jassh.jar ...
[info] Done packaging.
$ java -jar target/scala-2.11/jassh.jar
scala> SSH("localhost", "test").shell(_.echo("hello `whoami` at `date`")).trim
res4: String = hello test at Sun Mar 22 16:17:55 CET 2015

```

----

##hello world script##

It requires a local user named "test" with password "testtest", remember that you can remove the password, if your public key has been added in authorized_keys file of the test user.

````scala
#!/bin/sh
exec java -jar jassh.jar "$0" "$@"
!#
jassh.SSH.once("localhost", "test", "testtest") { ssh =>
  print(sh.execute("""echo "Hello World from `hostname`" """))
}
```

##Persisted shell session##

````scala
#!/bin/sh
exec java -jar jassh.jar "$0" "$@"
!#
jassh.SSH.shell("localhost", "test", "testtest") { sh =>
  import sh._
  println(s"initial directory is ${pwd}")
  cd("/tmp")
  println(s"now it is ${pwd}")
}
```

##Shell session to an SSH enabled  PowerShell Server (windows)
This functions much the same as a regular SSH connection, but many of the unix like commands are not supported and the terminal behaves differently
````scala
import fr.janalyse.ssh._

val settings = SSHOptions(host = host, username=user, password = pass, prompt = Some(prompt), timeout = timeout)
val session = SSH(settings)

val shell = session.newPowerShell

println(shell.ls)
println(shell.pwd)
```

##SSH Configuration notes##

To turn on/off ssh root direct access or sftp ssh subsystem.
```
    Subsystem       sftp    ...     (add or remove comment)
    PermitRootLogin yes or no       (of course take care of security constraints)
```

AIX SSHD CONFIGURATION :
```
    vi /system/products/openssh/conf/sshd_config
    /etc/rc.d/rc2.d/S99sshd reload
```

LINUX SSHD CONFIGURATION
```
    vi /etc/ssh/sshd_config
    /etc/init.d/sshd reload
```

SOLARIS SSHD CONFIGURATION
```
    vi /usr/local/etc/ssh/sshd_config
    svcadm restart ssh
```

MAC OS X CONFIGURATION
```
    sudo vi /etc/sshd_config
    sudo launchctl load -w /System/Library/LaunchDaemons/ssh.plist
```
