package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.ml.dt.spark.LogLevel.LogLevel
import org.apache.spark.{SparkConf, SparkContext}

object LogLevel extends Enumeration {
  type LogLevel = Value
  val ALL = Value("ALL")
  val DEBUG = Value("DEBUG")
  val ERROR = Value("ERROR")
  val FATAL = Value("FATAL")
  val TRACE = Value("TRACE")
  val WARN = Value("WARN")
  val INFO = Value("INFO")
  val OFF = Value("OFF")
}

object ContextFactory {
  private var sc: Option[SparkContext] = None

  def getContext(logLevel: LogLevel = LogLevel.INFO, appName: String = "Accident-Severity-Prediction", nThread: String = "*"): SparkContext =
    sc match {
      case Some(scc) => scc
      case None =>
        val conf = new SparkConf().setAppName(appName).setMaster("local[" + nThread + "]").set("spark.driver.maxResultSize", "0")
        sc = Some(new SparkContext(conf))
        sc.get.setLogLevel(logLevel.toString)
        sc.get
    }
}
