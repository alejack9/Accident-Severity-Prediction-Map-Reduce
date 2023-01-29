package it.unibo.scalable.ml.dt

import it.unibo.scalable.ml.dt.Utils.Types.Dataset

import scala.annotation.tailrec
import scala.collection.GenSeq

sealed trait Tree[T] {
  def toYaml(): String = {
    // nodes: (num_of_spaces, node, "-val: ..." string)
    // str: accumulator
    @tailrec
    def _toYml(nodes: GenSeq[(Int, Tree[T], String)], str: String): String = nodes match {
      case node :: xs => node match {
        // if it is a leaf, return a string with the "valString" and the leaf value
        case (spaces, Leaf(v), valString) => _toYml(xs, str + f"$valString${" " * spaces}leaf: ${v}\r\n")
        // if it is a CondNode, recall _toYml zipping the condition values (categories or continuous boundaries)
        // with the actual children and map them adding two spaces, the child and the "-val .." piece of string to
        // the current elaboration list (xs). Also add to the accumulator the value valString and the index of the
        // attribute in the condition followed by the "- children" header
        case (spaces, CondNode(cond, children), valString) =>
          _toYml(cond.getValues.zip(children).map{case (v, child) => (spaces + 2, child, f"${" " * spaces}- val: $v\r\n")} ++ xs,
            str + f"$valString${" " * spaces}index: ${cond.index}\r\n${" " * spaces}children:\r\n")
      }
      // if no more nodes => return str
      case Nil => str
    }

    _toYml(Seq((0, this, "")), "")
  }

  def predict[C <: Seq[Float]](data: Dataset[C]): GenSeq[T] = {
    def traverse(sample: C): T = {
      @tailrec
      def _traverse(tree: Tree[T]): T = {
        tree match {
          case Leaf(target) => target
          case CondNode(cond, children) =>
            val next = cond(sample)
            if(next == -1) return (-1.0f).asInstanceOf[T]
            _traverse(children(cond(sample)))
        }
      }

      _traverse(this)
    }

    data.map(traverse)
  }

  def score[C <: Seq[Float]](ds: Dataset[C]): Float = {
    val predictedYs = predict(ds.map(_.init))
    score(ds, predictedYs)
  }

  def score[C <: Seq[Float]](ds: Dataset[C], ys: GenSeq[T]): Float = {
    // right predictions / total sample
    ds.zip(ys).count { case (row, predicted) => row.last == predicted }.toFloat / ds.length
  }

  def ==(tree: Tree[T]): Boolean = {
    @tailrec
    def _eq(nodes: GenSeq[(Tree[T], Tree[T])]): Boolean = nodes match {
      case nodeTuple :: xs => nodeTuple._1 match {
        case Leaf(t) => nodeTuple._2 match {
          case CondNode(_,_) => false
          case Leaf(t1) => t1 == t
        }
        case CondNode(cond, children) => nodeTuple._2 match {
          case Leaf(_) => false
          case CondNode(cond1, children1) => {
            if (cond != cond1) return false
            _eq(children.zip(children1).toList ++ xs)
          }
        }
      }
      case Nil => true
    }

    _eq(Seq((tree, this)))
  }
}

case class CondNode[C, T](private val cond: Condition[C], private val children: GenSeq[Tree[T]]) extends Tree[T] {
  override def toString = f"CondNode(cond:(${cond}),children:[${children.mkString(", ")}])"

}

object LeafFactory {
  def get[T <: Seq[Float]](ds: Dataset[T]): Leaf[Float] = Leaf(ds.map(row => (row.last, 1)).groupBy(_._1).map{case (a, b) => (a, b.length)}.maxBy(_._2)._1)
}

case class Leaf[T](target: T) extends Tree[T] {
  override def toString = f"Leaf(${target})"
}
