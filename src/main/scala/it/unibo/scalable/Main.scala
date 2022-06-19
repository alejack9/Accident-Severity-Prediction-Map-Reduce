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
    val features = src.getLines.take(1).toList.head.split(",").map(_.trim)

    val data = ArrayBuffer[(Array[Float], Float)]()
    for (line <- src.getLines.drop(1).take(5000)){
      val row = line.split(',').map(_.trim)
      val toAdd = (row.tail.init.map(_.toFloat), row.tail.last.toFloat)
      data += toAdd
    }

    src.close()

    val featFormats = List(Format.Ordered,Format.Ordered,Format.Unordered,Format.Ordered,
      Format.Ordered,Format.Ordered,Format.Ordered,Format.Ordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Ordered,
      Format.Unordered,Format.Ordered,Format.Unordered,Format.Ordered,Format.Ordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Ordered,Format.Ordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,
      Format.Unordered,Format.Unordered,Format.Unordered,Format.Ordered,Format.Ordered,
      Format.Ordered,Format.Ordered,Format.Unordered)

    val DT = new ParDecisionTree[Float]()
    def placeholderMetric= (el:Array[Float]) => 0.1f
    val placeholderDepth = 90
    DT.build(data.toArray, features.zip(featFormats), placeholderMetric, placeholderDepth )
  }
}