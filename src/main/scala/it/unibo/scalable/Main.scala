package it.unibo.scalable

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import java.io.File
import it.unibo.scalable.ml.dt._
import it.unibo.scalable.ml.dt.Utils._

object Main {
  def main(args : Array[String]): Unit = {

    if (args.length == 0) {
      println("Training ds not provided")
      sys.exit(-1)
    }

    if (args.length == 1) {
      println("Test ds not provided")
      sys.exit(-1)
    }

    if (args.length == 2) {
      println("Computation mode not provided")
      sys.exit(-1)
    }

    val modes = Array("seq", "par")

    if (!modes.contains(args(2))) {
      println("Available computation modes: " + modes.mkString(", "))
      sys.exit(-1)
    }

    // test the alg with 1% -> 42876 samples , 5% -> 214380 samples and 10% -> 428759 samples of the original dataset,
    // val testSizeRates = Array(1, 5, 10)
    val trainDSPath = args(0)
    val testDSPath = args(1)

    val trainSrc = Source.fromFile(trainDSPath)
    val testSrc = Source.fromFile(testDSPath)

    val trainData = new ArrayBuffer[Seq[Float]]()
    val testData = new ArrayBuffer[Seq[Float]]()

    for (line <- trainSrc.getLines.drop(1))
      trainData += line.split(',').tail.map(_.trim.toFloat).toSeq

    for (line <- testSrc.getLines.drop(1))
      testData += line.split(',').tail.map(_.trim.toFloat).toSeq

    // read mode changed because it got overhead error
    //val x: Iterator[Seq[Float]] = trainSrc.getLines.drop(1).map(r => r.split(',').map(_.trim).tail.map(_.toFloat))
    val featFormats = List(
      Format.Continuous,Format.Continuous, Format.Categorical, Format.Continuous,Format.Continuous,Format.Continuous,Format.Continuous,
      Format.Continuous,Format.Categorical,Format.Categorical,Format.Categorical,Format.Continuous,Format.Categorical,
      Format.Continuous,Format.Categorical,Format.Continuous,Format.Categorical,Format.Continuous,Format.Continuous,Format.Categorical,
      Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,
      Format.Continuous,Format.Continuous,Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,
      Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,
      Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,Format.Categorical,
      Format.Categorical,Format.Categorical,Format.Categorical,Format.Continuous,Format.Continuous,Format.Continuous,Format.Continuous,
      Format.Categorical)

    val input = args(2) match {
      case "seq" => trainData.toArray.toSeq
      case "par" => trainData.toArray.toSeq.par
    }

    println("Computation mode: " + args(2))

    val c45 : C45Alg = new sequential.C45

    var t1 = System.nanoTime
    val tree = c45.train(input, featFormats) //.show
    val trainTime = System.nanoTime - t1

    t1 = System.nanoTime
    val predictedYs = tree.predict(testData)
    val testTime = System.nanoTime - t1

    val score = tree.score(testData, predictedYs)

    println("{" +
      "  trainTime: "   + trainTime / 1e9d +
      ", testTime: " + testTime / 1e9d +
      ", score: " + score +
      "}")

    TreeSaver.save(tree, trainDSPath + ".tree")

//    val sc = ContextFactory.getContext()
//
//    val rdd = sc.textFile(trainDSPath)
//    val dataset = rdd
////      .mapPartitionsWithIndex{(idx, iter) => if(idx == 0) iter.drop(1) else iter}
//      .map(row => row.split(",").toSeq)
//      .map(_.drop(1))
//      .map(_.map(_.toFloat))
////      .map(row => (row.reverse.head, row.reverse.tail))
////      .map{case (y, xs) => (y.toInt, xs.map(_.toFloat))}
//    print(dataset.collect.mkString("[\r\n", "\r\n", "]"))
//    val c45 = new C45
//    c45.run(dataset)
////    def giniIndex(ys: Seq[Float]): Float = 1 - (ys.distinct.map(c => math.pow(ys.count(_ == c) / ys.length.toFloat, 2).toFloat) aggregate 0.0.toFloat)((a, e) => a + e, _+_)
  }
}
