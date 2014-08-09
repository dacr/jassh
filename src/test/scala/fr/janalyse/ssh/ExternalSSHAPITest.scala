/*
 * Copyright 2014 David Crosson
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

package fr.janalyse.ssh.external

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ExternalSSHAPITest extends fr.janalyse.ssh.SomeHelp {

  info(s"Those tests require to have a user named '${sshopts.username}' with password '${sshopts.password}' on ${sshopts.host}")

  // -------------------------------------------------------------------
  // -- With a global import
  {
	  import jassh._
	
	  test("Hello 1") {
	    SSH.once(sshopts) { _.executeAndTrim("echo 'hello'") } should equal("hello")
	  }
	
	  test("Hello 2") {
	    SSH.shell(sshopts) { _.executeAndTrim("echo 'hello'") } should equal("hello")
	  }
	
	  test("Hello 3") {
	    import sshopts.{host, username=>user, password=>pass}
	    SSH.shell(host, user, password=pass) { _.executeAndTrim("echo 'hello'") } should equal("hello")
	  }
  }

  
  // -------------------------------------------------------------------
  // -- Without any jassh imports
  {
    test("Hello 4") {
      fr.janalyse.ssh.SSH.once(sshopts) { _.executeAllAndTrim(List("echo 'hello'")) } should equal(List("hello"))
    }
    test("Hello 5") {
      jassh.SSH.once(sshopts) { _.executeAllAndTrim(List("echo 'hello'")) } should equal(List("hello"))
    }
  }
  
  
}



