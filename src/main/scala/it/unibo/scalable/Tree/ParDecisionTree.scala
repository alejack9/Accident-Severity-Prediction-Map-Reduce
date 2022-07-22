package it.unibo.scalable.Tree

import it.unibo.scalable.Format.Format

class ParDecisionTree{
  var root: Tree[Float] = _

  var features: Array[(String, Format)] = _
  var metric: Array[(Float,Float)] => Float = _
  var target: String = _

  def build(data: Array[(Array[Float], Float)],
            features: Array[(String, Format)],
            target: String,
            metric: Array[(Float, Float)] => Float,
            depth: Int): Unit = {

    // number of features
    if (data.exists(sample => sample._1.length != features.length)) {
      throw new Exception("Given features format " + "(" + features.length + ")" +
        " doesn't match with amount of features in data " + "(" + data.head._1.length + ")")
    }

    this.features = features
    this.target = target
    this.metric = metric

    this.root = _build(data, depth)
  }

  // TODO GiniIndex
  private def _build(data: Array[(Array[Float], Float)],
                     depth: Int): Tree[Float] = {
    println()
    println("depth" + depth)
    println()

    if (depth == 0){
      return Leaf(data.head._2)
    }

    // TODO what if data is empty? Is it possible?
    if (Utility.isPure(data: Array[(Array[Float], Float)])) {
      // the target of the first elem is the same for each element
      Leaf(data.head._2)
    }
    else {
      val (featName, (splittingCriteria, metricValue)) = Utility.getBestSplitting(data, this.features, this.metric)
      val (leftData, rightData) = Utility.splitData(data, splittingCriteria)

      CondNode(splittingCriteria, _build(leftData, depth-1), _build(rightData, depth-1))
      }
    }


      //For each child node:
      //  If the child node is “pure” (has instances from only one class) tag it as a leaf and return.
      //  If not set the child node as the current node and recurse to step 2.

  }
