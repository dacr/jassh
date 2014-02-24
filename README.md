------------------------------------------------------------------
JASSH - JANALYSE-SSH - SCALA SSH API : README FILE
Crosson David - crosson.david@gmail.com
------------------------------------------------------------------

To turn on/off ssh root direct access or sftp ssh subsystem.
    Subsystem       sftp    ...     (add or remove comment)
    PermitRootLogin yes or no       (of course take care of security constraints)

AIX SSHD CONFIGURATION :
    vi /system/products/openssh/conf/sshd_config
    /etc/rc.d/rc2.d/S99sshd reload

LINUX SSHD CONFIGURATION
    vi /etc/ssh/sshd_config
    /etc/init.d/sshd reload

SOLARIS SSHD CONFIGURATION
    vi /usr/local/etc/ssh/sshd_config
    ???/lib/svc/method/sshd restart ???

