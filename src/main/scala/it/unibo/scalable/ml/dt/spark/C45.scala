package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.ml.dt.spark.Types.Dataset
import org.apache.spark.rdd.RDD

object Types {
  type Dataset = RDD[Seq[Float]]
}

class C45 {
  def run(D: Dataset) = {
    val dsLength = D.count()
    // 1st map-reduce: DATA PREPARATION
    // Extract attribute index, attribute value and class label from instance of the record
    val res = D.flatMap{row => row.init.indices.map(i => ((i, row(i), row.last), 1))}
      // Counts number of occurrences of combination for attribute index, value and class Label
      .reduceByKey(_+_)
      .map{case ((a,v,c), count) => ((a,v), (c, count))}
      .aggregateByKey(List[(Float, Int)]())(_:+_, _++_)
      .map{case (k, v) => (k, ((v aggregate 0)((acc, b) => acc + b._2, _+_), v))} // (k, (instancesOfAV, [(c, cnt)])
      .map{case (k, (instancesOfAV, classesCount)) =>
        val entropy = - (classesCount aggregate 0.0.toFloat)({case (acc, (_, cnt)) => acc + (cnt / instancesOfAV.toFloat) * math.log(cnt / instancesOfAV.toFloat).toFloat}, _+_)
        val info = (classesCount aggregate 0.0.toFloat)({case (acc, (_, cnt)) => acc + (cnt / instancesOfAV.toFloat) * entropy}, _+_)
        val splitInfo = - (instancesOfAV / dsLength.toFloat) * math.log(instancesOfAV / dsLength.toFloat).toFloat

        (k, (info, splitInfo))
      }
//      .aggregateByKey((0.0.toFloat,0.0.toFloat,0.0.toFloat))({case (acc, (info, splitInfo)) => (acc._1 + info, acc._2 + splitInfo, acc._3 + (- info / splitInfo) )}, (a,b) => (a._1 + b._1, a._2 + b._2, a._3 + b._3))
//      .sortBy({case (_, (_, _, c1)) => c1})


//    // Extract attribute index, attribute value and class label from instance of the record
//    val res = D.flatMap{row => row.init.indices.map(i => ((i, row(i), row.last), 1))}
//      // Counts number of occurrences of combination for attribute index, value and class Label
//      .reduceByKey(_+_)
//      .map{case ((i, v, c), count) => (i, (c, count))}
//      .groupByKey // i, [(c, count),(c1, count1),...]
//      .map{case (i, arr) => (i, arr, arr.aggregate(0)({ case (i1, (_, i2)) => i1 + i2 }, _+_))} // (attr, [(class, count)], all)
       // (attr, )

    print(res.collect.mkString("\r\n", "\r\n", ""))








//      .map{case ((a, c), count) => (a, (c, count))}
//      .groupByKey
//      // Find the best splitting attribute (decision node)
//      // Calculate Gain Ratio for each attribute
//      .map{ case (a, cs) => (a, (
//          - (cs.map{case(_, p) => (math.log(p) * p).toFloat} aggregate 0.0.toFloat)(_+_, _+_) // entropy
//        , information(a), splitInfomation(a)))
//      }


  }


}
