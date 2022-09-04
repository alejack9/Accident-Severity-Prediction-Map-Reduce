package it.unibo.scalable.ml.dt.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.funsuite.AnyFunSuite

class C45Test extends AnyFunSuite {
  val n_threads = "*"
  val conf: SparkConf = new SparkConf().setAppName("Accident-Severity-Prediction").setMaster("local[" + n_threads + "]").set("spark.driver.maxResultSize", "0")
  val sc = new SparkContext(conf)
//  val D: Types.Dataset = sc.parallelize(List(Array(1,1,10),Array(10,1,1),Array(8,1,13),Array(2,1,1),Array(5,1,1),Array(5,1,1),Array(1,1,2))).map(_.map(_.toFloat).toSeq)
  val D: Types.Dataset = sc.parallelize(List(Array(3,3,5,0), Array(1,1,7,0), Array(1,2,11,2), Array(1,9,11,2), Array(7,1,3,2))).map(_.map(_.toFloat).toSeq)

  val dtc = new C45

  test("run") {
    dtc.run(D)
    assert(1==1)
  }
}