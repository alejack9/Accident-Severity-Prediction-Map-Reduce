package it.unibo.scalable.ml.dt

sealed trait Tree[T] {
  def show: Unit = {
    def _show(tree: Tree[T], depth: Int): Unit = tree match {
      case Leaf(target) => println(depth + ": " + target)
      case CondNode(cond, children) =>
        println(depth + ": " + cond)
        children.foreach(child => _show(child, depth + 1))
    }
    _show(this, 0)
  }

  def ==(tree: Tree[T]): Boolean = tree match {
    case Leaf(target) => this match {
      case CondNode(_, _) => false
      case Leaf(target1) => target1 == target
    }
    case CondNode(cond, children) => this match {
      case Leaf(_) => false
      case CondNode(cond1, children1) =>
        if(cond != cond1) return false
        children1.zip(children).forall{case (c1, c2) => c1 == c2}
    }
  }
}

case class CondNode[T](cond: Condition, children: Seq[Tree[T]]) extends Tree[T]

case class Leaf[T](target: T) extends Tree[T]
