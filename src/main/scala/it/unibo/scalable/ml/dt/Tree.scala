package it.unibo.scalable.ml.dt

import it.unibo.scalable.ml.dt.Utils.Types.Dataset

import scala.annotation.tailrec
import scala.collection.GenSeq

sealed trait Tree[T] {

  def predict[C <: Seq[Float]](data: Dataset[C]): GenSeq[T] = {
    def traverse(sample: C): T = {
      @tailrec
      def _traverse(tree: Tree[T]): T = {
        tree match {
          case Leaf(target) => target
          case CondNode(cond, children) => _traverse(children(cond(sample)))
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

  def show(): Unit = {
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

case class CondNode[C, T](cond: Condition[C], children: GenSeq[Tree[T]]) extends Tree[T] {
  override def toString = f"CondNode(cond:(${cond}),children:[${children.mkString(", ")}])"
}

object LeafFactory {
  def get[T <: Seq[Float]](ds: Dataset[T]): Leaf[Float] = Leaf(ds.map(row => (row.last, 1)).groupBy(_._1).map{case (a, b) => (a, b.length)}.maxBy(_._2)._1)
}

case class Leaf[T](target: T) extends Tree[T] {
  override def toString = f"Leaf(${target})"
  // RIP
//  def this(ds: Dataset)(implicit ev: Float =:= T) = this(ev(ds.map(row => (row.last, 1)).groupBy(_._1).map{case (a, b) => (a, b.length)}.maxBy(_._2)._1))
}
