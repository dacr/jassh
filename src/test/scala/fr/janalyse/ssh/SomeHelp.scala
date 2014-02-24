package fr.janalyse.ssh

trait SomeHelp {
  val sshopts = SSHOptions("127.0.0.1", username = "test", password = "testtest")
  //val sshopts = SSHOptions("192.168.2.238", "test", password=Some("testtest"), port=22022)
  //val sshopts = SSHOptions("www.janalyse.fr")

  def f(filename: String) = new java.io.File(filename)

  def howLongFor[T](what: => T) = {
    val begin = System.currentTimeMillis()
    val result = what
    val end = System.currentTimeMillis()
    (end - begin, result)
  }

}