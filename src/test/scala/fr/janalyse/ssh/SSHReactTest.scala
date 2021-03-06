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

import org.scalatest.OptionValues._

class SSHReactTest extends SomeHelp {

  
  ignore("react attempts") {
    SSH.once(sshopts) { implicit ssh =>
      val sh = new SSHReact(timeout=5000L)
      
      sh.react("echo -n 'age='")
        .react("read age")
        .onFirst("age=", "32")
        .react("echo my age is $age")
        .consumeLine(line => false)
        
    }
  }

}

