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

package fr.janalyse.ssh

class ShellHistoryTest extends SomeHelp {
  
  test("shell disable history test") {
    SSH.shell(sshopts) {sh =>
      import sh._
      val hfiles=List(".bash_history")
      for {hfile <- hfiles if exists(hfile)} {
        val msgBefore = s"shell history before test $now"
        val msgAfter = s"shell history after test $now"
        sh.execute(s"echo $msgBefore")
        sh.execute("history 10 | grep 'shell history'") should include(msgBefore)
        disableHistory()
        sh.execute(s"echo $msgAfter")
        sh.execute("history 10 | grep 'shell history'") should not include(msgBefore)
        sh.execute("history 10 | grep 'shell history'") should not include(msgAfter)
      }
    }
  }  
  
  // TODO : improvements to be done within shell engine
  test("shell history test") {
    SSH.shell(sshopts) {sh =>
      import sh._
      sh.execute("history 10")
      whoami                  should equal(sshopts.username)
    }    
  }
  
}

