package fr.janalyse.ssh

import com.jcraft.jsch.{UserInfo, UIKeyboardInteractive}

/* Attention
 * - L'option PasswordAuthentication doit être à "yes" sinon impossible de s'authentifier
 *   (Configuration au niveau du serveur SSH) SSI on n'implemente pas "promptKeyboardInteractive"
 *
 */
case class SSHUserInfo(password: Option[String] = None, passphrase: Option[String] = None) extends UserInfo with UIKeyboardInteractive {
  override def getPassphrase(): String = passphrase getOrElse ""
  override def getPassword(): String = password getOrElse ""
  override def promptPassword(message: String) = true
  override def promptPassphrase(message: String) = true
  override def promptYesNo(message: String) = true
  override def showMessage(message: String): Unit = {}
  override def promptKeyboardInteractive(destination: String, name: String, instruction: String, prompt: Array[String], echo: Array[Boolean]): Array[String] = Array(getPassword())
}
