package it.unibo.scalable

import it.unibo.scalable.ml.dt.Format
import it.unibo.scalable.ml.dt.PLANET.Controller
import it.unibo.scalable.ml.dt.spark.C45
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.ml.classification.DecisionTreeClassifier

object Main {
  def main(args : Array[String]): Unit = {
    if (args.length == 0) {
      println("No args")
      return
    }
    val n_threads = "*"

    val datasetPath = args(0)
    val conf = new SparkConf().setAppName("Accident-Severity-Prediction").setMaster("local[" + n_threads + "]").set("spark.driver.maxResultSize", "0")
    val sc = new SparkContext(conf)

    val rdd = sc.textFile(datasetPath)
    val dataset = rdd
      .mapPartitionsWithIndex{(idx, iter) => if(idx == 0) iter.drop(1) else iter}
      .map(row => row.split(",").toSeq)
      .map(_.drop(1))
      .map(_.map(_.toFloat))
//      .map(row => (row.reverse.head, row.reverse.tail))
//      .map{case (y, xs) => (y.toInt, xs.map(_.toFloat))}
    print(dataset.collect.mkString("Array(\r\n", "\r\n", ")"))
    val c45 = new C45
    c45.run(dataset)
//    def giniIndex(ys: Seq[Float]): Float = 1 - (ys.distinct.map(c => math.pow(ys.count(_ == c) / ys.length.toFloat, 2).toFloat) aggregate 0.0.toFloat)((a, e) => a + e, _+_)
//    val controller = new Controller(_ < 10, giniIndex)
//    controller.run(dataset, List(Format.Ordered,Format.Ordered,Format.Unordered,Format.Ordered,Format.Ordered,Format.Ordered,Format.Ordered,Format.Ordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Ordered,Format.Unordered,Format.Ordered,Format.Unordered,Format.Ordered,Format.Ordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Ordered,Format.Ordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Unordered,Format.Ordered,Format.Ordered,Format.Ordered,Format.Ordered,Format.Unordered))
  }
}
