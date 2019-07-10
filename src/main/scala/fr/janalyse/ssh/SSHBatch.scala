package fr.janalyse.ssh

import language.implicitConversions

/**
 * SSHBatch class models ssh batch (in fact a list of commands)
 * @author David Crosson
 */
case class SSHBatch(cmdList: Iterable[String])

/**
 * SSHBatch object implicit conversions container
 * @author David Crosson
 */
object SSHBatch {
  implicit def stringListToBatchList(cmdList: Iterable[String]): SSHBatch = new SSHBatch(cmdList)
}
