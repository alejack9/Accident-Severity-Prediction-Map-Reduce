package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.ml.dt.spark.Types.Dataset
import org.apache.spark.rdd.RDD

object Types {
  type Dataset = RDD[Seq[Float]]
}

class C45 {
  def run(D: Dataset): Unit = {
    val dsLength = D.count()
    // 1st map-reduce: DATA PREPARATION (one time)
    // Extract attribute index, attribute value and class label from instance of the record
    // out ((j, a_j), (sample_id, c))

    // Map attribute
    val mapAttributeRes: RDD[((Int, Float), (Long, Float))] = D.zipWithIndex.flatMap {
      case (row, sample_id) =>
        row.init.indices.map(i =>
          ((i, row(i)), (sample_id, row.last))
        )
    }

    // Reduce attribute
    val reduceAttributeRes: RDD[((Int, Float), (Float, Long))] = mapAttributeRes.map{ case ((j, aj), (sample_id, c)) => ((j, aj, c), sample_id) }
      .mapValues(_ => 1L)
      .reduceByKey(_+_)
      .map{ case ((j, aj, c), cnt) => ((j, aj), (c, cnt))}

    // attribute selection
    val reducePopulationRes: RDD[((Int, Float), Long)] = reduceAttributeRes
      .mapValues(_ => 1L)
      .reduceByKey(_+_)


  }
}
