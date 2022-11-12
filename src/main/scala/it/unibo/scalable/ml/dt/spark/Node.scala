package it.unibo.scalable.ml.dt.spark

sealed trait Node

case class Leaf(target: Float) extends Node()

case class Link(nextAttributeIndex: Int) extends Node()

