#JASSH - SCALA SSH API#

High level scala SSH API for easy and fast operations on remote servers.

This API is [JSCH](http://www.jcraft.com/jsch/) based. Interfaces are stable. Many helper functions are provided to simplify unix operations [ps, ls, cat, kill, find, ...](http://www.janalyse.fr/scaladocs/janalyse-ssh/#fr.janalyse.ssh.ShellOperations), an other goal of this API is to create an unix abstraction layer (Linux, Aix, Solaris, Darwin, ...).

[**JAnalyse software maven repository**](http://www.janalyse.fr/repository/)

[**Scala docs**](http://www.janalyse.fr/scaladocs/janalyse-ssh)

**Current releases** :  **0.9.13** (for scala 2.10 and 2.11)   **0.9.5b3** (for scala 2.9.1, 2.9.2)

**Declare dependency in SBT as follow** :
```
libraryDependencies += "fr.janalyse"   %% "janalyse-ssh" % "0.9.13" % "compile"
```

**Add JAnalyse repository in SBT as follow** :
```
resolvers += "JAnalyse Repository" at "http://www.janalyse.fr/repository/"
```

----

##hello world script##

It requires a local user named "test" with password "testtest", remember that you can remove the password, if your public key has been added in authorized_keys file of the test user.

````scala
#!/bin/sh
exec java -jar jassh.jar "$0" "$@"
!#
jassh.SSH.shell("localhost", "test", "testtest") { sh =>
  print(sh.execute("""echo "Hello World from `hostname`" """))
}
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
    ???/lib/svc/method/sshd restart ???
```
