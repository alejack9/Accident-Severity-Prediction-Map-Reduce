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
  def calcEntropy(in: RDD[((Int, Float), (Float, Long, Long))]): Float = {
    val classesCounts = in
      .map {case ((j, _), (c, cnt, _)) => ((j, c), cnt)}
      .reduceByKey(_ + _)
      .map {case ((j, c), classCount) => (j, (c, classCount))} // for each j feat, the count of each class
      .groupByKey
      .take(1)
      .head
      ._2

    val dsLength = classesCounts.aggregate(0L)(_+_._2, _+_)

    println(classesCounts.mkString("(", ", ", ")") + ")")
    println(f"dsLength: $dsLength")

    - classesCounts.map(_._2).map(_ / dsLength.toFloat).map(p => p * math.log(p)).sum.toFloat
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
      .map { case ((j, aj), (sample_id, c)) => ((j, aj, c), sample_id) }
      .mapValues(_ => 1L)
      .reduceByKey(_ + _)
      .map { case ((j, aj, c), cnt) => ((j, aj), (c, cnt)) }

    println("====== Reduce Attribute Res ======")
    println(reduceAttributeRes.collect.mkString("(", ", ", ")\r\n"))

    // attribute selection
    val reducePopulationRes: RDD[((Int, Float), Long)] = reduceAttributeRes
      .mapValues(_ => 1L)
      .reduceByKey(_ + _)

    println("====== Reduce Population Res ======")
    println(reducePopulationRes.collect.mkString("(", ", ", ")\r\n"))

    val mapComputationInput: RDD[((Int, Float), (Float, Long, Long))] = reduceAttributeRes
      .join(reducePopulationRes)
      .mapValues { case ((c, cnt), all) => (c, cnt, all) }

    println("====== Map Computation Input ======")
    println(mapComputationInput.collect.mkString("(", ", ", ")\r\n"))

    val entropy = calcEntropy(mapComputationInput)
    println("====== Entropy ======")
    println(entropy)

//  TODO: CAPIRE SE E` GIUSTO QUESTO O IL CODICE VECCHIO
//        VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV

    // Entropy(S_v)
    // ((j, aj1), (1, ...)), ((j, aj2), (1, ...)), ((j, aj), (2, ...))
//    val mapComputationInputWithPartEntropy = mapComputationInput
    val mapComputationInputWithEntropy = mapComputationInput
      .mapValues { case (c, cnt, all) =>
        (c, cnt, all, (cnt / all.toFloat * math.log(cnt / all.toFloat)).toFloat) }
      .aggregateByKey((0L, 0f))({ case (acc, (_, _, all, part)) => (all, acc._2 + part) }, { case ((all, p1), (_, p2)) => (all, p1 + p2) })

//    val attributesEntropy = mapComputationInputWithPartEntropy
//      .aggregateByKey(0f)({ case (acc, (_, _, _, part)) => acc + part }, _ + _)

//    println("-------------- Attributes Entropy ------------")
//    println(attributesEntropy.collect.mkString("(", ", ", ")\r\n"))

//    val mapComputationInputWithEntropy = attributesEntropy
//      .join(mapComputationInputWithPartEntropy)
//      .map { case (k, (entropy, (c, cnt, all, _))) => (k, (c, cnt, all, entropy)) }
//      .mapValues(t => (t._3, t._4))
////      .reduceByKey { case ((all, ent1), (_, ent2)) => (all, ent1 + ent2) }
//      .mapValues(t => (t._1, -t._2))

//        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    println("========== mapComputationInputWithEntropy ==========")
    println(mapComputationInputWithEntropy.collect.mkString("(", ", ", ")\r\n"))

//    System.exit(-1)

    // ((j, aj), (all, entropy))
    // all: amount of instances with j = aj
    // entropy->  entropy of subset of instances with j = aj
    val mapComputationInputWithInfoAndSplitInfo = mapComputationInputWithEntropy
      .mapValues { case (all, entropy) =>
        (all / dsLength.toFloat * entropy, - all / dsLength.toFloat * math.log(all / dsLength.toFloat))
      }

    // input: ((j, aj), (info(j, aj), splitinfo(j, aj))
    // Gain(a, T) = Dataset entropy - Info(a, T)
    val reduceComputationWithInfoAndSplitInfoForJ = mapComputationInputWithInfoAndSplitInfo
      .map { case ((j, _), (info, splitInfo)) => (j, (info, splitInfo)) }
      .foldByKey((0, 0))((acc,infoSplitInfo) => (acc._1 + infoSplitInfo._1, acc._2 + infoSplitInfo._2))

    println("====== reduceComputationWithInfoAndSplitInfoForJ ======")
    println(reduceComputationWithInfoAndSplitInfoForJ.collect.mkString("(", ", ", ")\r\n"))

    val mapComputationWithGainRatio = reduceComputationWithInfoAndSplitInfoForJ.mapValues{ case (info, splitInfo) => (entropy - info) / splitInfo }

    println("====== mapComputationWithGainRatio ======")
    println(mapComputationWithGainRatio.collect.mkString("(", ", ", ")\r\n"))


    val bestAttribute = mapComputationWithGainRatio.reduce((res1, res2) => if (res1._2 > res2._2) res1 else res2)

    println("====== Reduce Computation Info And Split Info For J ======")
    println(mapComputationInputWithInfoAndSplitInfo.collect.mkString("(", ", ", ")\r\n"))
    println(reduceComputationWithInfoAndSplitInfoForJ.collect.mkString("(", ", ", ")\r\n"))
    println(mapComputationWithGainRatio.collect.mkString("(", ", ", ")\r\n"))
    println(bestAttribute)
  }
}
