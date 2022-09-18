package it.unibo.scalable

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.io.Source
import it.unibo.scalable.ml.dt.sequential.C45
import it.unibo.scalable.ml.dt.sequential.Format

//import it.unibo.scalable.ml.dt.spark.{C45, ContextFactory}

object Main {
  def main(args : Array[String]): Unit = {
    if (args.length == 0) {
      println("No args")
      return
    }

    val datasetPath = args(0)

    val src = Source.fromFile(datasetPath)

    val x: Iterator[Seq[Float]] = src.getLines.slice(1, 1000).map(r => r.split(',').map(_.trim).tail.map(_.toFloat))

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

    val y = x.toSeq
    val c45 = new C45
    val t1 = System.nanoTime
    val dt_time = c45.run(y, featFormats)
    val t2 = System.nanoTime() - t1

//    val dt = new C45().run(x.toSeq, featFormats)
    dt_time.show

    println(t2 / 1e9d)

//
//    val sc = ContextFactory.getContext()
//
//    val rdd = sc.textFile(datasetPath)
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
