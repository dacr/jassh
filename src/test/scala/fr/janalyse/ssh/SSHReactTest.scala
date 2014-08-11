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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.OptionValues._

@RunWith(classOf[JUnitRunner])
class SSHReactTest extends SomeHelp {

  //==========================================================================================================
  test("react attempts") {
    SSH.once(sshopts) { implicit ssh =>
      val sh = new SSHReact()
      try {
        val buf = new StringBuilder() // Warn : mutable
        def ageInteractor(nextChar:Int, prod:sh.Producer) {
          buf.append(nextChar.toChar)
          if (buf.endsWith("age=")) prod.send("32")
        }
        val result = sh.react("""echo -n "age=" ; read age ; echo my age is $age""", Some(ageInteractor))
        
        info(result)
        result.split("\n").last should equal("my age is 32")
        
      } finally {
        sh.close
      }
    }
  }

}

