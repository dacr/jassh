package fr.janalyse.ssh

import language.implicitConversions

/**
 * SSHPassword class models a password, that may be given or not
 * @author David Crosson
 */
case class SSHPassword(password: Option[String]) {
  override def toString: String = password getOrElse ""
}

/**
 * NoPassword object to be used when no password is given
 * @author David Crosson
 */
object NoPassword extends SSHPassword(None)

/**
 * SSHPassword object implicit conversions container
 * @author David Crosson
 */
object SSHPassword {
  implicit def string2password(pass: String): SSHPassword = pass match {
    case "" => NoPassword
    case password => SSHPassword(Some(pass))
  }
  implicit def stringOpt2password(passopt: Option[String]): SSHPassword = passopt match {
    case Some(password) => string2password(password)
    case None => NoPassword
  }
}

