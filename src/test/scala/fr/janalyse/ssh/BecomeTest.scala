/*
 * Copyright 2013 David Crosson
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

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import scala.io.Source
import scala.util.Properties
import java.io.File
import java.io.IOException
import scala.collection.parallel.ForkJoinTaskSupport
import org.scalatest.OptionValues._

@RunWith(classOf[JUnitRunner])
class BecomeTest extends FunSuite with ShouldMatchers with SomeHelp {

  test("become tests") {
    import util.Properties.{userName=>user}
    info("For this test to success, you must allow current user to be able ")
    info("  to log on localhost without password authentication")
    info("(use ssh-keygen to generate your ssh keys, if not already done)")
    info("cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys")
    info("ssh localhost     # Should not ask you a password")

    val opts = SSHOptions("127.0.0.1", username=user, timeout=10000)
    SSH.shell(opts) {sh =>
      sh.become("test", Some("testtest")) should equal(true)
      sh.whoami should equal("test")
    }
  }
  
}

