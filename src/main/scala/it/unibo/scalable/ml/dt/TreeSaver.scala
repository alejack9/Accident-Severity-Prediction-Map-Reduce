package it.unibo.scalable.ml.dt

import java.io._
import scala.annotation.tailrec

object TreeSaver {

  @tailrec
  private def saveTree[T](nodes: Seq[(Int, Tree[T])], pw: PrintWriter): Unit = nodes match {
    case x :: xs => x match {
      case (depth, CondNode(cond, children)) =>
        pw.write(f"$depth - ${cond.toString} | ")
        saveTree(children.map(t => (depth + 1, t)) ++ xs, pw)
      case (depth, Leaf(value)) =>
        pw.write(f"$depth - ${value.toString} | ")
        saveTree(xs, pw)
    }
    case Nil =>
  }

  def save[T](tree: Tree[T], pw: PrintWriter): Unit = {
    saveTree(List((0, tree)), pw)
    pw.close()
  }

  def save[T](tree: Tree[T], dest: String): Unit = {
    val pw = new PrintWriter(new File(dest))
    save(tree, pw)
  }
}
