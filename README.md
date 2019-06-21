# JASSH - the SCALA SSH API  [![Build Status][travisImg]][travisLink] [![License][licenseImg]][licenseLink] [![Codacy][codacyImg]][codacyLink] [![codecov][codecovImg]][codecovLink]

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

In your build.sbt, add this (available in maven central) :
```
libraryDependencies += "fr.janalyse"   %% "janalyse-ssh" % version
```
_(starting from 0.10, java 8 bytecodes are used, and scala 2.10, 2.11 and 2.12 are supported)_

Latest `version`: [![Maven][mavenImg]][mavenLink] [![Scaladex][scaladexImg]][scaladexLink]


[**Scala docs**](http://www.janalyse.fr/scaladocs/janalyse-ssh)


[mavenImg]: https://img.shields.io/maven-central/v/fr.janalyse/janalyse-ssh_2.12.svg
[mavenImg2]: https://maven-badges.herokuapp.com/maven-central/fr.janalyse/janalyse-ssh_2.12/badge.svg
[mavenLink]: https://search.maven.org/#search%7Cga%7C1%7Cfr.janalyse.janalyse-ssh

[scaladexImg]: https://index.scala-lang.org/dacr/jassh/janalyse-ssh/latest.svg
[scaladexLink]: https://index.scala-lang.org/dacr/jassh

[licenseImg]: https://img.shields.io/github/license/dacr/jassh.svg
[licenseImg2]: https://img.shields.io/:license-apache2-blue.svg
[licenseLink]: LICENSE

[codacyImg]: https://img.shields.io/codacy/a335d839f49646389d88d02c01e0d6f6.svg
[codacyImg2]: https://api.codacy.com/project/badge/grade/a335d839f49646389d88d02c01e0d6f6
[codacyLink]: https://www.codacy.com/app/dacr/jassh/dashboard

[codecovImg]: https://img.shields.io/codecov/c/github/dacr/jassh/master.svg
[codecovImg2]: https://codecov.io/github/dacr/jassh/coverage.svg?branch=master
[codecovLink]: http://codecov.io/github/dacr/jassh?branch=master

[travisImg]: https://img.shields.io/travis/dacr/jassh.svg
[travisImg2]: https://travis-ci.org/dacr/jassh.png?branch=master
[travisLink]:https://travis-ci.org/dacr/jassh


----

## hello world script

It requires a local user named "test" with password "testtest", remember that you can remove the password, if your public key has been added in authorized_keys file of the test user.

```scala
#!/bin/sh
exec java -jar jassh.jar "$0" "$@"
!#
jassh.SSH.once("localhost", "test", "testtest") { ssh =>
  print(sh.execute("""echo "Hello World from $(hostname)" """))
}
```

## Persisted shell session

```scala
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

## Shell session to an SSH enabled  PowerShell Server (windows)
This functions much the same as a regular SSH connection, but many of the unix like commands are not supported and the terminal behaves differently
```scala
import fr.janalyse.ssh._

val settings = SSHOptions(host = host, username=user, password = pass, prompt = Some(prompt), timeout = timeout)
val session = SSH(settings)

val shell = session.newPowerShell

println(shell.ls)
println(shell.pwd)
```

## SSH Configuration notes

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
