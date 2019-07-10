package fr.janalyse.ssh

import com.jcraft.jsch.ProxyHTTP
import org.slf4j.LoggerFactory


sealed trait EndPoint {
  val host:String
  val port:Int
}

case class ProxyEndPoint(host:String,
                         port:Int=ProxyEndPoint.defaultPort
                        ) extends EndPoint
object ProxyEndPoint {
  val defaultPort=3128
}

case class SshEndPoint(host:String,
                       username:String=SshEndPoint.defaultUserName,
                       port:Int=SshEndPoint.defaultPort
                      ) extends EndPoint
object SshEndPoint {
  val defaultUserName=scala.util.Properties.userName
  val defaultPort=22
}


case class AccessPath(name:String, endpoints:List[EndPoint])


/**
  * Abstraction layer over complex ssh connections (direct, proxytunnel, intricated ssh tunnels)
  * Everything is done lazily, already established connections are cached and automatically checked
  * @param accesses
  */
class SSHConnectionManager(accesses:List[AccessPath]) {
  val logger = LoggerFactory.getLogger(getClass)
  val accessesByName=accesses.groupBy(_.name).mapValues(_.head)

  case class Bounce(localEndPoint:SshEndPoint, associatedSSH:SSH)
  private var bouncers = Map.empty[List[EndPoint], Bounce]
  private var sshers = Map.empty[List[EndPoint], SSH]

  def pooledIntricate[T](access:AccessPath)(that: SSH => T):T = synchronized {
    val aname = access.name
    // TODO : manage resiliency, autoclose
    def worker(remainingEndPoints: Iterable[EndPoint],
               previousPosition: List[EndPoint]=Nil,
               localEndPoint: Option[SshEndPoint] = None,
               through: Option[ProxyEndPoint] = None): SSH = {
      remainingEndPoints.headOption match {
        // ----------------------------------------------------------------
        case Some(endpoint: ProxyEndPoint) =>
          val currentPosition = previousPosition:+endpoint
          worker(remainingEndPoints.tail, currentPosition, localEndPoint, Some(endpoint))
        // ----------------------------------------------------------------
        case Some(endpoint: SshEndPoint) if localEndPoint.isDefined => // intricate tunnel
          val currentPosition = previousPosition:+endpoint
          val bounce = bouncers.get(currentPosition) match {
            case Some(bounce) =>
              if (logger.isDebugEnabled()) logger.debug(s"$aname : reuse bounce for ${endpoint.username}@${endpoint.host}~${endpoint.port}")
              bounce
            case None =>
              if (logger.isDebugEnabled()) logger.debug(s"$aname : create bounce for ${endpoint.username}@${endpoint.host}~${endpoint.port}")
              val proxy = through.map(p => new ProxyHTTP(p.host, p.port))
              val opts = SSHOptions(localEndPoint.get.host, username = localEndPoint.get.username, port = localEndPoint.get.port, proxy = proxy)
              val ssh = SSH(opts)
              val newPort = ssh.remote2Local(endpoint.host, endpoint.port)
              val newLocalEndPoint = SshEndPoint("127.0.0.1", username = endpoint.username, port = newPort)
              val bounce = Bounce(newLocalEndPoint, ssh)
              bouncers += currentPosition->bounce
              bounce
          }
          worker(remainingEndPoints.tail, currentPosition, Some(bounce.localEndPoint))
        // ---------------------------------------------------------------->
        case Some(endpoint: SshEndPoint) => // first tunnel
          val currentPosition = previousPosition:+endpoint
          val bounce = bouncers.get(currentPosition) match {
            case Some(bounce) =>
              if (logger.isDebugEnabled()) logger.debug(s"$aname : reuse bounce for ${endpoint.username}@${endpoint.host}~${endpoint.port}")
              bounce
            case None =>
              if (logger.isDebugEnabled()) logger.debug(s"$aname : create bounce for ${endpoint.username}@${endpoint.host}~${endpoint.port}")
              val proxy = through.map(p => new ProxyHTTP(p.host, p.port))
              val opts = SSHOptions(endpoint.host, username = endpoint.username, port = endpoint.port, proxy = proxy)
              val ssh = SSH(opts)
              val newPort = ssh.remote2Local("127.0.0.1", 22)
              val newLocalEndPoint = SshEndPoint("127.0.0.1", username = endpoint.username, port = newPort)
              val bounce = Bounce(newLocalEndPoint, ssh)
              bouncers += currentPosition->bounce
              bounce
          }
          worker(remainingEndPoints.tail, currentPosition, Some(bounce.localEndPoint))

        // ----------------------------------------------------------------
        case None if localEndPoint.isDefined =>
          val position = previousPosition

          sshers.get(position) match {
            case None =>
              val endpoint = localEndPoint.get
              if (logger.isDebugEnabled()) logger.debug(s"$aname : create terminal endpoint for ${endpoint.username}@${endpoint.host}~${endpoint.port}")
              val opts=SSHOptions(endpoint.host, username=endpoint.username, port=endpoint.port)
              val ssh = SSH(opts)
              sshers += position->ssh
              ssh
            case Some(ssh) =>
              if (logger.isDebugEnabled()) logger.debug(s"$aname : reuse terminal endpoint for ${ssh.options.username}@${ssh.options.host}~${ssh.options.port}")
              ssh
          }

        // ----------------------------------------------------------------
        case None =>
          throw new RuntimeException("Empty ssh path")
      }
    }

    val ssh = worker(access.endpoints)
    that(ssh)
  }


  def ssh[T](name:String) (withSSH: SSH => T):Option[T] = {
    accessesByName.get(name).map{ access =>
      //SSHConnectionManager.intricate(access) { withSSH }
      pooledIntricate(access)(withSSH)
    }
  }

  def shell[T](name:String) (withShell : SSHShell => T):Option[T] = {
    accessesByName.get(name).map{ access =>
      //SSHConnectionManager.intricate(access) { ssh => ssh.shell(withShell) }
      pooledIntricate(access)(_.shell(withShell))
    }
  }

  def close():Unit = {
    for {bouncer <- bouncers.values} {
      try {bouncer.associatedSSH.close()} catch {
        case ex:Exception =>
      }
    }
    for {ssher <- sshers.values} {
      try {ssher.close} catch {
        case ex:Exception =>
      }
    }
  }

  override def finalize(): Unit = {
    try {
      close()
    } finally {
      super.finalize()
    }
  }
}

object SSHConnectionManager {
  def apply(accesses:List[AccessPath]):SSHConnectionManager = new SSHConnectionManager(accesses)

  /**
    * rebuild the connection access path (proxytunnels, intricated tunnels) each time it is called and
    * execute that code. Here the intrication is kept only while executing the given lambda !
    * @param access access path specification
    * @param that lambda to execute
    * @tparam T result type returned by the lambda expression
    * @return lambda result
    */
  def intricate[T](access:AccessPath)(that: SSH => T):T = {
    def worker(endpoints: Iterable[EndPoint],
               localEndPoint: Option[SshEndPoint] = None,
               through: Option[ProxyEndPoint] = None): T = {
      endpoints.headOption match {
        case Some(endpoint: ProxyEndPoint) =>
          worker(endpoints.tail, localEndPoint, Some(endpoint))
        // ----------------------------------------------------------------
        case Some(endpoint: SshEndPoint) if localEndPoint.isDefined => // intricate tunnel
          val proxy = through.map(p => new ProxyHTTP(p.host, p.port))
          val opts = SSHOptions(localEndPoint.get.host, username = endpoint.username, port = localEndPoint.get.port, proxy = proxy)
          SSH.once(opts) { ssh =>
            val newPort = ssh.remote2Local(endpoint.host, endpoint.port)
            val newLocalEndPoint = SshEndPoint("127.0.0.1", username = endpoint.username, port = newPort)
            worker(endpoints.tail, Some(newLocalEndPoint))
          }
        // ----------------------------------------------------------------
        case Some(endpoint: SshEndPoint) => // first tunnel
          val proxy = through.map(p => new ProxyHTTP(p.host, p.port))
          val opts = SSHOptions(endpoint.host, username = endpoint.username, port = endpoint.port, proxy = proxy)
          SSH.once(opts) { ssh =>
            val newPort = ssh.remote2Local("127.0.0.1", 22)
            val newLocalEndPoint = SshEndPoint("127.0.0.1", username = endpoint.username, port = newPort)
            worker(endpoints.tail, Some(newLocalEndPoint))
          }
        // ----------------------------------------------------------------
        case None if localEndPoint.isDefined =>
          val opts = SSHOptions(localEndPoint.get.host, username = localEndPoint.get.username, port = localEndPoint.get.port)
          SSH.once(opts) {
            that
          }
        // ----------------------------------------------------------------
        case None =>
          throw new RuntimeException("Empty ssh path")
      }
    }
    worker(access.endpoints)
  }
}
