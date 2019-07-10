package fr.janalyse.ssh

import org.slf4j._

trait SSHLazyLogging {
  val logger: Logger = LoggerFactory.getLogger(getClass)
}
