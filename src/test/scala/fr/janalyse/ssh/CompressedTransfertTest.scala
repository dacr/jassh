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

import java.io.File
import org.scalatest.OptionValues._
import scala.io.Source

class CompressedTransfertTest extends SomeHelp {

  info(s"Those tests require to have a user named '${sshopts.username}' with password '${sshopts.password}' on ${sshopts.host}")

  test("simple") {
    val content = "Hello world"
    val testedfile = "testme-tobecompressed.txt"
    val gztestedfile = testedfile + ".gz"
    val gztestedfileMD5 = "38570c70a362855368dd8c5f25a157f7"

    SSH.ftp(sshopts) { _.put(content, testedfile) }
    SSH.ftp(sshopts) { _.get(testedfile) } should equal(Some(content))
    def doclean = {
      new File(testedfile).delete()
      new File(gztestedfile).delete()
    }
    // Now let's test the compressed feature
    doclean
    SSH.once(sshopts) { ssh =>
      ssh.receive(testedfile, testedfile)
      Source.fromFile(testedfile).getLines should equal(content)
      ssh.receiveNcompress(testedfile, testedfile)
      new File(gztestedfile).exists should equal(true)
      ssh.localmd5sum(gztestedfile) should equal(Some(gztestedfileMD5))
    }
    doclean
    SSH.shellAndFtp(sshopts) { (_, ftp) =>
      ftp.receive(testedfile, testedfile)
      Source.fromFile(testedfile).getLines should equal(content)
      ftp.receiveNcompress(testedfile, testedfile)
      new File(gztestedfile).exists should equal(true)
      ftp.localmd5sum(gztestedfile) should equal(Some(gztestedfileMD5))
    }
    doclean
    SSH.ftp(sshopts) { ftp =>
      ftp.receive(testedfile, testedfile)
      Source.fromFile(testedfile).getLines should equal(content)
      ftp.receiveNcompress(testedfile, testedfile)
      new File(gztestedfile).exists should equal(true)
      ftp.localmd5sum(gztestedfile) should equal(Some(gztestedfileMD5))
    }
    doclean
  }

}
