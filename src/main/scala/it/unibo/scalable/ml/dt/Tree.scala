package it.unibo.scalable.ml.dt

trait Tree[T] { }

case class CondNode[T](cond: Seq[Float] => Int, children: Seq[Tree[T]]) extends Tree[T]

case class Leaf[T](target: T) extends Tree[T]
