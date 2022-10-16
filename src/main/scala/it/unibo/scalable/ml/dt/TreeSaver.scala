package it.unibo.scalable.ml.dt

import java.io._
import net.liftweb.json._
import net.liftweb.json.Serialization.write

object TreeSaver {

  def save[T](tree: Tree[T], dest: String): Unit = {

    implicit val formats = DefaultFormats

    val pw = new PrintWriter(new File(dest))
//    pw.write(tree.toString)
    pw.write(write(tree))

    pw.close()
  }
}
