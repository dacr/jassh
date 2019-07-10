package fr.janalyse.ssh

import java.io.File

trait AllOperations extends ShellOperations with TransfertOperations {

    /**
     * Recursively get a remote directory to a local destination
     * @param remote remote path, file or directory.
     * @param dest local destination directory, it it doesn't exist then it is created
     */
    def rreceive(remote:String, dest:File): Unit = {
      def worker(curremote: String, curdest: File):Unit = {
        if (isDirectory(curremote)) {
          for {
            found <- ls(curremote)
            newremote = curremote + "/" + found
            newdest = new File(curdest, found)
          } {
            curdest.mkdirs
            worker(curremote=newremote, curdest=newdest)
          }
        } else receive(curremote, curdest)
      }
      worker(curremote=remote, curdest=dest)
    }

    /**
     * Recursively send a local directory to a remote destination
     * @param src local path, file or directory
     * @param remote remote destination directory, if it doesn't exist then it is created
     */
    def rsend(src:File, remote:String): Unit = {
      def worker(cursrc: File, curremote: String): Unit = {
        if (cursrc.isDirectory) {
          for {
            found <- cursrc.listFiles
            newsrc = new File(cursrc, found.getName)
            newremote = curremote + "/" + found.getName
          } {
            mkdir(curremote)
            worker(cursrc=newsrc, curremote=newremote)
          }
        } else send(cursrc, curremote)
      }
      worker(cursrc=src, curremote=remote)
    }

}