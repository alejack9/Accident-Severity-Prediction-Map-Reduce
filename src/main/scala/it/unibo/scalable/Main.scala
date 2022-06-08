package it.unibo.scalable

import it.unibo.scalable.knn.Knn
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Main {
  def main(args : Array[String]): Unit = {
    if (args.length == 0) {
      println("Non ci sono argomenti")
      return
    }
    val n_threads = "*"

    val datasetPath = args(0)
    val conf = new SparkConf().setAppName("KNN").setMaster("local[" + n_threads + "]").set("spark.driver.maxResultSize", "0")
    val sc = new SparkContext(conf)

    val rdd = sc.textFile(datasetPath)
  }
}