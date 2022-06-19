package it.unibo.scalable.Tree

trait Tree[T] {}

case class CondNode[T](cond: Array[T] => Boolean, left: Tree[T], right: Tree[T]) extends Tree[T] {}
case class Leaf[T](target:T) extends Tree[T] {}