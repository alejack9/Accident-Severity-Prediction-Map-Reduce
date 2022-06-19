package it.unibo.scalable.Tree

import it.unibo.scalable.Tree.Tree
import it.unibo.scalable.Format.Format

class ParDecisionTree[T] {
  var root: Tree[Float] = _
  var features: Array[(String, Format)] = _
  var metric: Array[T] => Float = _

  def build(data: Array[(Array[T], T)],
            features: Array[(String, Format)],
            metric: Array[T] => Float,
            depth: Int): Unit = {

    this.features = features
    this.metric = metric

    this.root = _build(data, depth)
  }

  private def _build(data: Array[(Array[T], T)],
                     depth: Int): Tree[T] = {
    throw new NotImplementedError()
  }
  }
