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

trait Process {
    val pid: Int
    val ppid: Int
    val user: String
    val cmdline: String
    private val tokens = cmdline.split("""\s+""").toList filter { _.size > 0 }
    val cmd = tokens.head
    val args = tokens.tail.toList
  }
  
  case class ProcessTime(days:Int, hours:Int, minutes:Int, seconds:Int) {
    val ellapsedInS = days*24*3600 + hours*3600 + minutes*60 + seconds
  }
  object ProcessTime {
    def apply(spec:String):ProcessTime = {
      val re1="""(\d+)""".r
      val re2="""(\d+):(\d+)""".r
      val re3="""(\d+):(\d+):(\d+)""".r
      val re4="""(\d+)-(\d+):(\d+):(\d+)""".r
      spec match {
        case re1(s) => ProcessTime(0,0,0,s.toInt)
        case re2(m,s) => ProcessTime(0,0,m.toInt,s.toInt)
        case re3(h,m,s) => ProcessTime(0,h.toInt,m.toInt,s.toInt)
        case re4(d,h,m,s) => ProcessTime(d.toInt,h.toInt,m.toInt,s.toInt)
        case _ => ProcessTime(0,0,0,0)
      }
    }
  }
  
  trait ProcessState {
    val name:String
  }
  
  case class LinuxProcessState(
      name:String,
      extra:String
  ) extends ProcessState
  
  object LinuxProcessState{
        val states = Map(
           'D'->"UninterruptibleSleep",
           'R'->"Running",
           'S'->"InterruptibleSleep",
           'T'->"Stopped",
           'W'->"Paging", //   paging (not valid since the 2.6.xx kernel)
           'X'->"Dead",
           'Z'->"Zombie"
        ) 

    def fromSpec(spec:String):LinuxProcessState = {
      val name = spec.headOption.flatMap(states get _) getOrElse "UnknownState"
      val extra = if (spec.size >0) spec.tail else ""
      new LinuxProcessState(name, extra)
    }
  }
  
  
  case class DarwinProcessState(
      name:String,
      extra:String
  ) extends ProcessState

  object DarwinProcessState {
        val states = Map(
               'I'->"Idle",//       Marks a process that is idle (sleeping for longer than about 20 seconds).
               'R'->"Running",//       Marks a runnable process.
               'S'->"Sleeping",//       Marks a process that is sleeping for less than about 20 seconds.
               'T'->"Stopped",//       Marks a stopped process.
               'U'->"UninterruptibleSleep",//       Marks a process in uninterruptible wait.
               'Z'->"Zombie"//       Marks a dead process (a ``zombie'').

        ) 

    def fromSpec(spec:String):DarwinProcessState = {
      val name = spec.headOption.flatMap(states get _) getOrElse "UnknownState"
      val extra = if (spec.size >0) spec.tail else ""
      new DarwinProcessState(name, extra)
    }
  }
  
  
  case class AIXProcess(
        pid: Int,
        ppid: Int,
        user: String,
        cmdline: String
  ) extends Process
  
  case class SunOSProcess(
        pid: Int,
        ppid: Int,
        user: String,
        cmdline: String
  ) extends Process
  
  
  case class LinuxProcess(
        pid: Int,
        ppid: Int,
        user: String,
        state:LinuxProcessState,
        rss: Int,             // ResidentSizeSize (Ko)
        vsz: Int,             // virtual memory size of the process (Ko)
        etime: ProcessTime,   // Ellapsed time since start   [DD-]hh:mm:ss
        cputime: ProcessTime, // CPU time used since start [[DD-]hh:]mm:ss
        cmdline: String
  ) extends Process

  
  case class DarwinProcess(
        pid: Int,
        ppid: Int,
        user: String,
        state:DarwinProcessState,
        rss: Int,             // ResidentSizeSize (Ko)
        vsz: Int,             // virtual memory size of the process (Ko)
        etime: ProcessTime,   // Ellapsed time since start   [DD-]hh:mm:ss
        cputime: ProcessTime, // CPU time used since start [[DD-]hh:]mm:ss
        cmdline: String
  ) extends Process

