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
class TimeoutTest extends FunSuite with ShouldMatchers with SomeHelp  {

  info(s"Those tests require to have a user named '${sshopts.username}' with password '${sshopts.password}' on ${sshopts.host}")
  
  test("timeout tests") { // TODO : not working, make timeout possible with too long running remote command; (^C is already possible)!!
    val opts = sshopts.copy(timeout=7000, connectTimeout=2000)
    SSH.once(opts) {ssh =>
      ssh.executeAndTrim("sleep 4; echo 'ok'") should equal("ok")
      intercept[SSHTimeoutException] {
        ssh.executeAndTrim("sleep 10; echo 'ok'")
      }
    }
  }
  
  test("timeout tests with shell SSH session") { // TODO : not working, make timeout possible with too long running remote command; (^C is already possible)!!
    val opts = sshopts.copy(timeout=7000, connectTimeout=2000)
    SSH.shell(opts) {sh =>
      sh.executeAndTrim("sleep 4; echo 'ok'") should equal("ok")
      intercept[SSHTimeoutException] {
        sh.executeAndTrim("sleep 10; echo 'ok'")
      }
      sh.executeAndTrim("echo 'good'") should equal("good")
    }
  }
  
}

