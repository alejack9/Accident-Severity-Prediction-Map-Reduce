package it.unibo.scalable.ml.dt.spark

import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.funsuite.AnyFunSuite

class C45Test extends AnyFunSuite {
  val n_threads = "*"
  val conf: SparkConf = new SparkConf().setAppName("Accident-Severity-Prediction").setMaster("local[" + n_threads + "]").set("spark.driver.maxResultSize", "0")
  val sc: SparkContext = ContextFactory.getContext(LogLevel.OFF)
  val D: Types.Dataset = sc.parallelize(List(
    Seq(3, 3, 5, 0),
    Seq(1, 1, 7, 0),
    Seq(1, 2, 11, 2),
    Seq(1, 9, 11, 2),
    Seq(7, 1, 3, 2)
  ).map(_.map(_.toFloat)))

  val D1: Types.Dataset = sc.parallelize(List(
    Seq(53, 3, 5, 1),
    Seq(1, 1, 1, 1),
    Seq(1, 1, 9, 1),
    Seq(90, 2, 1, 0),
    Seq(1, 55, 11, 0),
    Seq(1, 9, 11, 1),
    Seq(0, 1, 3, 5),
    Seq(8, 77, 42, 1),
    Seq(2, 32, 57, 2),
    Seq(245, 0, 24, 0),
  ).map(_.map(_.toFloat)))


  val dtc = new C45

  test("get best attribute") {
    dtc.getBestAttribute(D, sc.broadcast(D.first.indices))
  }

  test("train") {
    println(dtc.train(D).mkString("\r\n"))
  }

  test("train  10 times") {
    for {
      i <- Range.inclusive(1, 10)
    } {
      println(dtc.train(D).mkString("\r\n"))
      println(f"__________________{$i}_____________________")
    }
  }

  test("score") {
    println(Evaluator.score(D, Evaluator.predict(dtc.train(D), D)))
  }

  test("toYamlD") {
    val treeTable = dtc.train(D)
    assertResult("index: 2\r\nchildren:\r\n- val: 3.0\r\n  leaf: 2.0\r\n- val: 11.0\r\n  leaf: 2.0\r\n- val: 7.0\r\n  leaf: 0.0\r\n- val: 5.0\r\n  leaf: 0.0\r\n")(Evaluator.toYaml(treeTable))
  }

  test("toYamlD1") {
    val treeTable = dtc.train(D1)
    assertResult("index: 0\r\nchildren:\r\n- val: 1.0\r\n  index: 1\r\n  children:\r\n  - val: 55.0\r\n    leaf: 0.0\r\n  - val: 9.0\r\n    leaf: 1.0\r\n  - val: 1.0\r\n    leaf: 1.0\r\n- val: 245.0\r\n  leaf: 0.0\r\n- val: 2.0\r\n  leaf: 2.0\r\n- val: 90.0\r\n  leaf: 0.0\r\n- val: 0.0\r\n  leaf: 5.0\r\n- val: 8.0\r\n  leaf: 1.0\r\n- val: 53.0\r\n  leaf: 1.0\r\n")(Evaluator.toYaml(treeTable))
  }
}
