package fr.janalyse.ssh

import com.jcraft.jsch.ProxyHTTP


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

  val accessesByName=accesses.groupBy(_.name).mapValues(_.head)


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

  def ssh[T](name:String) (withSSH: SSH => T):Option[T] = {
    accessesByName.get(name).map{ access =>
      intricate(access) { withSSH }
    }
  }

  def shell[T](name:String) (withShell : SSHShell => T):Option[T] = {
    accessesByName.get(name).map{ access =>
      intricate(access) { ssh =>
        ssh.shell(withShell)
      }
    }
  }
}

object SSHConnectionManager {
  def apply(accesses:List[AccessPath]):SSHConnectionManager = new SSHConnectionManager(accesses)
}
