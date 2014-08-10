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
class ShellOperationsTest extends SomeHelp {
    
  //==========================================================================================================
  test("helper methods") {
    val testfile="sshapitest.dummy"
    val testdir="sshapitest-dummydir"
    val started = now
    
    SSH.shell(sshopts) {sh =>
      
      // create a dummy file and dummy directory
      sh.execute("echo -n 'toto' > %s".format(testfile))
      sh.execute("mkdir -p %s".format(testdir))
      val homedir = sh.executeAndTrim("pwd")
      val rhostname = sh.executeAndTrim("hostname")
      
      // now tests the utilities methods
      import sh._
      uname.toLowerCase       should (equal("linux") or equal("darwin") or equal("aix") or equal("sunos"))
      osname                  should (equal("linux") or equal("darwin") or equal("aix") or equal("sunos"))
      whoami                  should equal(sshopts.username)
      osid                    should (equal(Linux) or equal(Darwin) or equal(AIX) or equal(SunOS))
      arch                    should not be 'empty
      env.size                should be > (0)
      hostname                should equal(rhostname)
      fileSize(testfile)      should equal(Some(4))
      md5sum(testfile)        should equal(Some("f71dbe52628a3f83a77ab494817525c6"))
      md5sum(testfile)        should equal(Some(SSHTools.md5sum("toto")))
      sha1sum(testfile)       should equal(Some("0b9c2625dc21ef05f6ad4ddf47c5f203837aa32c"))
      ls                      should contain(testfile)
      cd(testdir)
      pwd                     should equal(homedir+"/"+testdir)
      cd
      pwd                     should equal(homedir)
      sh.test("1 = 1")        should equal(true)
      sh.test("1 = 2")        should equal(false)
      isFile(testfile)        should equal(true)
      isDirectory(testfile)   should equal(false)
      exists(testfile)        should equal(true)
      exists(testdir)         should equal(true)
      isExecutable(testfile)  should equal(false)
      findAfterDate(".", started).size should (be >=(1) and be <=(3)) // because of .bash_history
      val reftime = now.getTime
      date().getTime          should (be>(reftime-5000) and be<(reftime+5000))
      fsFreeSpace("/tmp")     should be('defined)
      fileRights("/tmp")      should be('defined)
      ps().filter(_.cmdline contains "java").size should be >(0)
      du("/bin").value        should be >(0L)
      cat(testfile)           should include("toto")
      rm(testfile)
      notExists(testfile)     should equal(true)
      rmdir(testdir)
      notExists(testdir)      should equal(true)
    }
  }
  
  test("shell disable history test") {
    SSH.shell(sshopts) {sh =>
      import sh._
      val hfiles=List(".bash_history")
      for {hfile <- hfiles if exists(hfile)} {
        val msgBefore = s"shell history before test $now"
        val msgAfter = s"shell history after test $now"
        sh.execute(s"echo $msgBefore")
        sh.execute("history | grep 'shell history'") should include(msgBefore)
        disableHistory()
        sh.execute(s"echo $msgAfter")
        sh.execute("history | grep 'shell history'") should not include(msgBefore)
        sh.execute("history | grep 'shell history'") should not include(msgAfter)
      }
    }
  }  
  
  // TODO : something wrong is happening with travis test platform
  ignore("last modified tests") {
    val testfile="sshapitestZZ.dummy"
    SSH.shell(sshopts) {sh =>
      import sh._
      // create a dummy file and dummy directory
      sh.execute("echo -n 'toto' > %s".format(testfile))
      val testfilereftime = now.getTime

      val lm = lastModified(testfile).map(_.getTime)
      lm.value should (be>(testfilereftime-5000) and be<(testfilereftime+5000)) 

      rm(testfile)
    }
  }
   
  // TODO : improvements to be done within shell engine
  ignore("shell history test") {
    SSH.shell(sshopts) {sh =>
      import sh._
      sh.execute("history")
      whoami                  should equal(sshopts.username)
    }    
  }

  
  test("more ls test") {
    SSH.shell(sshopts) {sh =>
      sh.execute("rm -fr ~/truc")
      sh.mkdir("truc")
      sh.ls("truc").size should equal(0)
      sh.rmdir("truc"::Nil)
    }
  }

  
}

