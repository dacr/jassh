package fr.janalyse.ssh

import scala.util.{ Properties => SP }
import java.io.File.{ separator => FS, pathSeparator => PS }
import com.jcraft.jsch._

/**
 * SSHIdentity
 */
case class SSHIdentity(
    privkey:String,
    passphrase:SSHPassword=NoPassword //If not set, will use the default global passphrase if available 
    )


/**
 * SSHOptions stores all ssh parameters
 * @author David Crosson
 */
case class SSHOptions(
  host:String="localhost",
  username: String = util.Properties.userName,
  password: SSHPassword = NoPassword,
  passphrase: SSHPassword = NoPassword,
  name: Option[String] = None,
  port: Int = 22,
  prompt: Option[String] = None,
  timeout: Long = 0,
  connectTimeout: Long = 30000,
  retryCount: Int = 5,
  retryDelay: Int = 2000,
  identities: List[SSHIdentity]=SSHOptions.defaultIdentities,
  charset: String = "ISO-8859-15",
  noneCipher: Boolean = true,
  compress: Option[Int] = None,
  execWithPty:Boolean = false,    // Sometime some command doesn't behave the same with or without tty, cf mysql
  ciphers:Array[String]="none,aes128-cbc,aes192-cbc,aes256-cbc,3des-cbc,blowfish-cbc,aes128-ctr,aes192-ctr,aes256-ctr".split(","),
  proxy:Option[Proxy]=None
  ) {
  //val keyfiles2lookup = sshKeyFile ++ List("id_rsa", "id_dsa") // ssh key search order (from sshUserDir)
  def compressed = this.copy(compress=Some(5))
  def viaProxyHttp(host:String, port:Int=80) = this.copy(proxy = Some(new ProxyHTTP(host,port)))
  def viaProxySOCKS4(host:String, port:Int=1080) = this.copy(proxy = Some(new ProxySOCKS4(host,port)))
  def viaProxySOCKS5(host:String, port:Int=1080) = this.copy(proxy = Some(new ProxySOCKS5(host,port)))
  def addIdentity(identity:SSHIdentity) = this.copy(identities = identity::identities)
}

object SSHOptions {
  val defaultPrivKeyFilenames=List(
      "identity",
      "id_dsa",
      "id_ecdsa",
      "id_ed25519",
      "id_rsa"
      )
  val defaultIdentities=
    defaultPrivKeyFilenames
       .map(SP.userHome + FS + ".ssh" + FS + _)
       .map(SSHIdentity(_))
}