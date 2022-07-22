package it.unibo.scalable.Tree

import it.unibo.scalable.Tree.Utility.TreeCond

trait Tree[T] {}

case class CondNode[T](cond: TreeCond, left: Tree[T], right: Tree[T]) extends Tree[T] {}
case class Leaf[T](target:T) extends Tree[T] {}