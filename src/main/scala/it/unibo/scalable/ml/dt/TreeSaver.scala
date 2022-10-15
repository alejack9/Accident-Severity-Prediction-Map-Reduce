package it.unibo.scalable.ml.dt

import java.io._

object TreeSaver {

  def save[T](tree: Tree[T], dest: String): Unit = {
    val pw = new PrintWriter(new File(dest))
    pw.write(tree.toString)
    pw.close()
  }
}
