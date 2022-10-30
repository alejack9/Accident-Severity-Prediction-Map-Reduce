package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.ml.dt.spark.Types.Dataset
import org.apache.spark.rdd.RDD

object Types {
  type Dataset = RDD[Seq[Float]]
}

// ########################
// ## Cursor Parking lot ##
// ##                    ##
// ########################

class C45 {
  //                     j , a_j       c     cnt   all
  def entropy(in: RDD[((Int, Float), (Float, Long, Long))]): Float = {
    val classesCounts = in
      .map {case ((j, aj), (c, cnt, all)) => ((j, c), cnt)}
      .reduceByKey(_ + _)
      .map {case ((j, c), classCount) => (j, (c, classCount))} // for each j feat, the count of each class
      .groupByKey
      .take(1)
      .head

    val dsLength = classesCounts._2.aggregate(0L)(_+_._2, _+_)

    println("( " + classesCounts._1 + " , " + classesCounts._2.mkString("(", ", ", ")") + ")")
    println(f"dsLength: $dsLength")

    // dsLength
    // per ogni classe c -> cnt c
//    val dsLength: Long = in.map{case ((j, _), (_, _, all)) => (j, all) }
//      .reduceByKey(_+_)
//      .take(1)
//      .head._2

//    in.map { case (_, (c, cnt, all)) => (c, cnt)}
//      .reduceByKey(_+_)
//      .mapValues { case(cnt, all) => (cnt / all) * math.log(cnt / all) }
////    in.map {case ((),(c, cnt, all)) = cnt/all}
    0f
  }



  def run(D: Dataset): Unit = {
    val dsLength = D.count()
    // 1st map-reduce: DATA PREPARATION (one time)
    // Extract attribute index, attribute value and class label from instance of the record
    // out ((j, a_j), (sample_id, c))

    // Map attribute
    val mapAttributeRes: RDD[((Int, Float), (Long, Float))] = D
      .zipWithIndex
      .flatMap {
        case (row, sample_id) =>
          row.init.indices.map(i =>
            ((i, row(i)), (sample_id, row.last))
          )
      }

    println("====== Map Attribute Res ======")
    println(mapAttributeRes.collect.mkString("(", ", ", ")\r\n"))

    // Reduce attribute
    val reduceAttributeRes: RDD[((Int, Float), (Float, Long))] = mapAttributeRes
      .map{ case ((j, aj), (sample_id, c)) => ((j, aj, c), sample_id) }
      .mapValues(_ => 1L)
      .reduceByKey(_+_)
      .map{ case ((j, aj, c), cnt) => ((j, aj), (c, cnt))}

    println("====== Reduce Attribute Res ======")
    println(reduceAttributeRes.collect.mkString("(", ", ", ")\r\n"))

    // attribute selection
    val reducePopulationRes: RDD[((Int, Float), Long)] = reduceAttributeRes
      .mapValues(_ => 1L)
      .reduceByKey(_+_)

    println("====== Reduce Population Res ======")
    println(reducePopulationRes.collect.mkString("(", ", ", ")\r\n"))

    val mapComputationInput: RDD[((Int, Float), (Float, Long, Long))] = reduceAttributeRes
      .join(reducePopulationRes)
      .mapValues {case ((c, cnt), all) => (c, cnt, all)}

    println("====== Map Computation Input ======")
    println(mapComputationInput.collect.mkString("(", ", ", ")\r\n"))

    entropy(mapComputationInput)
//    val mapComputationRes = mapComputationInput.
  }
}
