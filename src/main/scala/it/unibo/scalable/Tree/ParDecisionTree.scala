package it.unibo.scalable.Tree

import it.unibo.scalable.Format.Format

class ParDecisionTree[T] {
  var root: Tree[T] = _
  var features: Array[(String, Format)] = _
  var metric: Array[(Array[T],T)] => Float = _

  def build(data: Array[(Array[T], T)],
            features: Array[(String, Format)],
            metric: Array[(Array[T], T)] => Float,
            depth: Int): Unit = {

    this.features = features
    this.metric = metric

    this.root = _build(data, depth)
  }

  // TODO GiniIndex
  private def _build(data: Array[(Array[T], T)],
                     depth: Int): Tree[T] = {
    if (depth == 0){
      return Leaf(data.head._2)
    }

    if (Utility.isPure(data: Array[(Array[T], T)])) {
      // the target of the first elem is the same for each element
      Leaf(data.head._2)
    }
    else {
      val (splittingCriteria, metricValue) = Utility.getBestSplitting(data, this.metric)
      val (leftData, rightData) = Utility.splitData(data, splittingCriteria)

      CondNode(splittingCriteria, _build(leftData, depth-1), _build(rightData, depth-1))
      }
    }


      //For each child node:
      //  If the child node is “pure” (has instances from only one class) tag it as a leaf and return.
      //  If not set the child node as the current node and recurse to step 2.

  }
