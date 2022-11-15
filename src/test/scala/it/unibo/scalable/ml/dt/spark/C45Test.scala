package it.unibo.scalable.ml.dt.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.funsuite.AnyFunSuite

class C45Test extends AnyFunSuite {
  val n_threads = "*"
  val conf: SparkConf = new SparkConf().setAppName("Accident-Severity-Prediction").setMaster("local[" + n_threads + "]").set("spark.driver.maxResultSize", "0")
  val sc = ContextFactory.getContext(LogLevel.OFF)
//  val D: Types.Dataset = sc.parallelize(List(Array(1,1,10),Array(10,1,1),Array(8,1,13),Array(2,1,1),Array(5,1,1),Array(5,1,1),Array(1,1,2))).map(_.map(_.toFloat).toSeq)
  val D: Types.Dataset = sc.parallelize(List(
    Seq(3, 3, 5, 0),
    Seq(1, 1, 7, 0),
    Seq(1, 2, 11, 2),
    Seq(1, 9, 11, 2),
    Seq(7, 1, 3, 2)
  ).map(_.map(_.toFloat)))

  val dtc = new C45

  test("get best attribute") {
    dtc.getBestAttribute(D)
    assert(1 == 1)
  }
  test("train") {
    for {
      i <- Range.inclusive(1, 10)
    } {
      println(dtc.newTrain(D).mkString("\r\n"))
      println(f"__________________{$i}_____________________")
    }
    assert(1 == 1)
  }
}
