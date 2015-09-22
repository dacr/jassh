
# TODO LIST

 - TODO : Add tests with a remote host, in addition to localhost
 - TODO : "ls --format=single-column" may to be replaced by "ls | cat"
 - TODO : Check if "date -u '+%Y-%m-%d %H:%M:%S %Z'" is supported on all *nix  (Checked OK for : AIX, Linux, SunOS)
 - TODO : BSD, Cygwin support
 - TODO : trying to find a solution with SCP and some special linux file (with unknown file size)
 - TODO : manage when sftp submodule is not available => throw the right exception
 - INPG : add sudo support / with password prompt recognition...
 - TODO : Fixing new bug linked to jsch and newer release of openssh :
     + [info]   com.jcraft.jsch.JSchException: Algorithm negotiation fail
     + [info]   at com.jcraft.jsch.Session.receive_kexinit(Session.java:583)
     + [info]   at com.jcraft.jsch.Session.connect(Session.java:320)
```
  => temporary Work around : change sshd_config and add supported
     jsck algo : KexAlgorithms diffie-hellman-group1-sha1
  => More information available here :
    http://stackoverflow.com/questions/26424621/algorithm-negotiation-fail-ssh-in-jenkins
    http://www.programmingforliving.com/2014/10/com.jcraft.jsch.JSchException-Algorithm-negotiation-fail.html
```

