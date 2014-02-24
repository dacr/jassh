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

package object jassh {
  type SSH = fr.janalyse.ssh.SSH
  val SSH = fr.janalyse.ssh.SSH
  type SSHOptions = fr.janalyse.ssh.SSHOptions
  val SSHOptions = fr.janalyse.ssh.SSHOptions
  //implicit def stringToCommand(cmd: String) = new fr.janalyse.ssh.SSHCommand(cmd)
  //implicit def stringListToBatchList(cmdList: Iterable[String]) = new fr.janalyse.ssh.SSHBatch(cmdList)
  //implicit def stringToRemoteFile(filename: String) = new fr.janalyse.ssh.SSHRemoteFile(filename)
}
