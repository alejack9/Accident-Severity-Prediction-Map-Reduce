package it.unibo.scalable

import it.unibo.scalable.Tree.ParDecisionTree

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.math.Fractional.Implicits.infixFractionalOps
import scala.math.Numeric.DoubleIsFractional.mkNumericOps

object Main {

  def main(args : Array[String]): Unit = {
    if (args.length == 0) {
      println("No args")
      return
    }
    val datasetPath = args(0)

    val src = Source.fromFile(datasetPath)

    // get features names (except for accident index) and target
    val allFeats = src.getLines.take(1).toList.head.split(",").map(_.trim).tail
    val features = allFeats.init
    val target = allFeats.last

    // get data as [[feature values], target]
    val data = ArrayBuffer[(Array[Float], Float)]()

    for (line <- src.getLines.drop(1).take(5000)){
      val row = line.split(',').map(_.trim)
      val toAdd = (row.tail.init.map(_.toFloat), row.tail.last.toFloat)
      data += toAdd
    }

    src.close()

    // define data formats used by splitting handler
    val featFormats = List(
      Format.Ordered,Format.Ordered, Format.Unordered, Format.Ordered,Format.Ordered,Format.Ordered,Format.Ordered,
      Format.Ordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Ordered,Format.Unordered,Format.Ordered,Format.Unordered,Format.Ordered,Format.Ordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Ordered,Format.Ordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Ordered,Format.Ordered,Format.Ordered,Format.Ordered,
      Format.Unordered)

    // create decision tree
    def placeholderMetric: Array[(Array[Float], Float)] => Float = (el:Array[(Array[Float], Float)]) => 0.1f
    val placeholderDepth = 5

    val DT = new ParDecisionTree[Float]()
    DT.build(data.toArray, features.zip(featFormats), target, placeholderMetric, placeholderDepth)

    println(DT.root)
  }
}