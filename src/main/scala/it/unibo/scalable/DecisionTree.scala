package it.unibo.scalable

import scala.collection.mutable.ArrayBuffer
import it.unibo.scalable.Utility

class DecisionTree {
  def createLeaf(target: Array[Float]): Float = {
    target.max
  }

  def createChild(node: Map[String, Any], max_depth: Int, min_size: Int, depth: Int): Unit = {
    val left = node.get("subsets")
    val right = node(1)

    if
  }

  def buildTree(train: ArrayBuffer[Array[Float]], max_depth: Int, min_size: Int): Map[String, Any] = {
    val root = Utility.get_best_split(train, 4)
    createChild(root, max_depth, min_size, 1)
    root
  }
}
