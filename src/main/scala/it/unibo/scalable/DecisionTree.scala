package it.unibo.scalable

import scala.collection.mutable.ArrayBuffer

class DecisionTree {
  def createLeaf(target: ArrayBuffer[Array[Float]]): Float = {
    val outcomes = target.map(el => el(target.length - 1))
    outcomes.max
  }

  def checkChild(child: ArrayBuffer[Array[Float]], node: ArrayBuffer[Any], max_depth: Int, min_size: Int, depth: Int) = {
    if(child.length <= min_size) {
      node :+ createLeaf(child)
    }
    else {
      node :+ Utility.get_best_split(child, child.length - 1)
      createChild(node(node.length - 1).asInstanceOf[ArrayBuffer[Any]], max_depth, min_size, depth)
    }
  }

  def createChild(node: ArrayBuffer[Any], max_depth: Int, min_size: Int, depth: Int): Unit = {
    val left = node(2).asInstanceOf[ArrayBuffer[ArrayBuffer[Array[Float]]]](0)
    val right = node(2).asInstanceOf[ArrayBuffer[ArrayBuffer[Array[Float]]]](1)

    if(left.nonEmpty || right.nonEmpty) {
      node :+ createLeaf(left ++ right)
      node :+ createLeaf(left ++ right)
      return
    }

    if(depth >= max_depth) {
      node :+ createLeaf(left)
      node :+ createLeaf(right)
      return
    }

    checkChild(left, node, max_depth, min_size, depth+1)
    checkChild(right, node, max_depth, min_size, depth+1)
  }

  def buildTree(train: ArrayBuffer[Array[Float]], max_depth: Int, min_size: Int): ArrayBuffer[Any] = {
    val root = Utility.get_best_split(train, 4)
    createChild(root, max_depth, min_size, 1)
    root
  }
}
