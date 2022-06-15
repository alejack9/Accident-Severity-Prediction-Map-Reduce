package it.unibo.scalable

import scala.collection.mutable.ArrayBuffer

object Utility {
  def gini_index(subsets: ArrayBuffer[ArrayBuffer[Array[Float]]], classes: Array[Float], targetIndex: Int): Double = {
    var n_instances = 0.0
    subsets.foreach{g => n_instances += g.length}

    var gini = 0.0

    subsets.foreach{g =>
      val size = g.length

      if(size == 0) {
        var score = 0.0
        classes.foreach{c => val targetValues = g.map{row => row(targetIndex)}
          val p = targetValues.count(r => r == c) / size
          score += p*p
        }

        gini += (1.0 - score) * (size / n_instances)
      }
    }

    gini
  }

  // divido il dataset in input in due subdataset
  def testSplit(index: Int, value: Float, dataset: ArrayBuffer[Array[Float]]): ArrayBuffer[ArrayBuffer[Array[Float]]] = {
    val left = ArrayBuffer[Array[Float]]()
    val right = ArrayBuffer[Array[Float]]()

    dataset.foreach{row => if(row(index) < value) left += row else right += row}

    ArrayBuffer(left, right)
  }

  def get_best_split(dataset: ArrayBuffer[Array[Float]], targetIndex: Int): Map[String, Any] = {
    val classes = Array[Float]()
    var bestIndex = 0
    var bestScore = 999.0
    var subsets = ArrayBuffer[ArrayBuffer[Array[Float]]]()
    var bestSubsets = ArrayBuffer[ArrayBuffer[Array[Float]]]()
    var bestValue, gini = 0.0

    dataset.foreach{row => classes :+ row(targetIndex)}

    for{(row, index) <- dataset.view.zipWithIndex} {
      subsets = testSplit(index, row(index), dataset)
      gini = gini_index(subsets, classes, 4)
      if (gini < bestScore) {
        bestIndex = index
        bestValue = row(index)
        bestScore = gini
        bestSubsets = subsets
      }
    }

    Map("index" -> bestIndex, "value" -> bestValue, "subsets" -> bestSubsets)
  }
}
