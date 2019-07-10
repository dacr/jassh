/*
 * Copyright 2014-2015 David Crosson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.janalyse.ssh

import org.scalatest.FunSuite
import scala.io.Source
import scala.util.Properties
import java.io.File
import java.io.IOException
import org.scalatest.OptionValues._

class StabilityTest extends SomeHelp {

  //==========================================================================================================
  test("stability test") {
    info("Will fail  with OpenSSH_6.9p1-hpn14v5, OpenSSL 1.0.1p 9 Jul 2015 (gentoo kernel 4.0.5)")
    info("Will fail  with OpenSSH_6.9p1-hpn14v5, OpenSSL 1.0.2d 9 Jul 2015 (gentoo kernel 4.0.5)")
    info("Won't fail with OpenSSH_6.2p2, OSSLShim 0.9.8r 8 Dec 2011 (Mac OS X 10.10.5)")
    val max = 200
    var failed = 0
    val sshopts = SSHOptions("127.0.0.1", username = "test", password = "testtest")
    for { x <- 1 to max } {
      try {
        SSH.once(sshopts) { _.execute("true") }
      } catch {
        case ex: Exception =>
          info(s"Failed on '$ex' for #$x / $max")
          failed += 1
      }
    }
    info(s"Failed $failed times for $max attempts (${(failed * 100d / max).toInt}%)")
    failed should equal(0)
  }
}

