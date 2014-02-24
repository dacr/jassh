package fr.janalyse.ssh

class SSHTimeoutException(val stdout:String, val stderr:String) extends Exception("SSH Timeout") {

}