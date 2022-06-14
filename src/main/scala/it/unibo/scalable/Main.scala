package it.unibo.scalable

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scala.io.Source
import scala.collection.mutable.ArrayBuffer

object Main {
  def main(args : Array[String]): Unit = {
//    if (args.length == 0) {
//      println("No args")
//      return
//    }
//    val n_threads = "*"
//
//    val datasetPath = args(0)
//    val conf = new SparkConf().setAppName("Accident-Severity-Prediction").setMaster("local[" + n_threads + "]").set("spark.driver.maxResultSize", "0")
//    val sc = new SparkContext(conf)
//
//    val rdd = sc.textFile(datasetPath)

    val filename = "D:\\Universita\\Magistrale\\2_Anno\\Scalable and Cloud Programming\\Progetto\\Accident-Severity-Prediction-Map-Reduce\\data\\preprocessed.csv"
    val bufferedSource = Source.fromFile(filename)
    val dataset = ArrayBuffer[Array[String]]()

    for(line <- bufferedSource.getLines.drop(1).take(150)) {
      dataset += line.split(",").map(_.trim)
    }

    print(dataset(0)(4))
  }
}