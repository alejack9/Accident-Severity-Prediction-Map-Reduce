package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.MathExtension
import it.unibo.scalable.ml.dt.spark.Types.Dataset
import org.apache.spark.rdd.RDD

import scala.collection.immutable.HashMap

object Types {
  type Dataset = RDD[Seq[Float]]
}

// The chosen data structure that represents the DT is a hashmap where
// - key: list of tuples (feat index, feat value) representing the path for the node
// - value: node object. I can be a Link(index of the splitting feature) or Leaf(target class)
class C45{
  def train(D: Dataset): Map[List[(Int, Float)], Node] = {
    def _train(dataset: Dataset, path: List[(Int, Float)], treeTable: Map[List[(Int, Float)], Node], level: Int): Map[List[(Int, Float)], Node] = {
      // get the best attribute index with the related gain ratio
      val bestAttrIndex = getBestAttribute(dataset)

      if ( math.abs(bestAttrIndex._2) == 0.0 // If the best chosen gain ratio is 0.0 then there are no more splits that carry info
         || bestAttrIndex._2.isNaN )// NaN means that the subset has 1 sample only
      {
//        println(f"for best attribute: ${bestAttrIndex._1} with gain ratio ${bestAttrIndex._2} creating a leaf ${getClass(dataset)}")
        return treeTable + (path -> Leaf(getClass(dataset)))
      }

      // get all the distinct values in the dataset for the best chosen feature
      val bestAttrValues = dataset.map(_(bestAttrIndex._1)).distinct.collect

      // for each possible value, create a subnode and update the tree table
      bestAttrValues
        .map(value => {
          val current = (bestAttrIndex._1, value)

          if (path.contains(current)) // if this couple (feature, value) already exists in the node path
            treeTable + ((path :+ current) -> Leaf(getClass(dataset)))
           else {
//            println(f"for best attribute: ${bestAttrIndex._1} with gain ratio ${bestAttrIndex._2} creating a link ($bestAttrIndex._2}")
             _train(
              dataset.filter(_ (bestAttrIndex._1) == value), // the subset
              path :+ current, // the path of the subset node
              treeTable + (path -> Link(bestAttrIndex._1)), // the tree table updated with the current node as a link
              level + 1
            )
          }
        })
        .reduce(_ ++ _)
    }
    _train(D, List.empty, HashMap.empty, 0)
  }

  // Entropy(D) = - sum(p(D, c) * log2(p(D,c)) for each class c
  def calcEntropy(in: RDD[((Int, Float), (Float, Long, Long))]): Float = {
  //                         j ,  a_j       c     cnt   all
    val classesCounts = in
      .map {case ((j, _), (c, cnt, _)) => ((j, c), cnt)}
      .reduceByKey(_ + _)
      .map {case ((j, c), classCount) => (j, (c, classCount))} // for each j feat, the count of each class
      .groupByKey
      .take(1)
      .head
      ._2

    // sum each class count to obtain the total number of samples in the DS
    val dsLength = classesCounts.aggregate(0L)(_+_._2, _+_)

    - classesCounts.map(_._2).map(_ / dsLength.toFloat).map(p => {p * MathExtension.log2(p)}).sum.toFloat
  }

  def getClass(D: Dataset): Float = {
    // take the class with most occurrences
    D.map(_.last).countByValue().maxBy(_._2)._1
  }

  def getBestAttribute(D: Dataset): (Int, Double) = {
    val dsLength = D.count()

    // 1st map-reduce step: DATA PREPARATION
    // out ((j, a_j), (class, count))
    val dataPreparationRes: RDD[((Int, Float), (Float, Long))] = D
      // Map attribute step
      .zipWithIndex // attach the row id
      .flatMap {
        case (row, sample_id) =>
          row.init.indices.map(i =>
            ((i, row(i)), (sample_id, row.last))
          )}
      // Reduce attribute step
      .map { case ((j, aj), (sample_id, c)) => ((j, aj, c), 1L) }
//      .mapValues(_ => 1L)
      .reduceByKey(_ + _)
      .map { case ((j, aj, c), cnt) => ((j, aj), (c, cnt)) }

//    println("====== Data preparation Res ======")
//    println(dataPreparationRes.collect.mkString("(", ", ", ")\r\n"))

    // 2nd map-reduce step: ATTRIBUTE SELECTION
    // reduce population
    val reducePopulationRes: RDD[((Int, Float), Long)] = dataPreparationRes
      .aggregateByKey(0L)({ case (acc, (_, cnt)) => acc + cnt }, _+_)

//    println("====== Reduce Population Res ======")
//    println(reducePopulationRes.collect.mkString("(", ", ", ")\r\n"))

    // add all (number of samples for with j == aj)
    val mapComputationInput: RDD[((Int, Float), (Float, Long, Long))] = dataPreparationRes
      .join(reducePopulationRes).mapValues { case ((c, cnt), all) => (c, cnt, all) }

//    println("====== Map Computation Input ======")
//    println(mapComputationInput.collect.mkString("(", ", ", ")\r\n"))

    // calc general entropy of the set D, useful in the calculation of the gain ratio
    val entropy = calcEntropy(mapComputationInput)
//    println("====== Entropy ======")
//    println(entropy)

    // -------------- info and split info calc for each j aj (partial bc we want them for j ---------------------
    val mapComputationInputWithPartialInfoAndSplitInfo: RDD[((Int, Float), (Double, Double))] = mapComputationInput           //   p for entropy       p for info
      .map {case  ((j, aj), (_, cnt, all)) => ((j, aj), (cnt/all.toFloat * MathExtension.log2(cnt/all.toFloat), all / dsLength.toFloat)) }
//      .mapValues {case (pEntropy, pInfo) =>  (pEntropy * MathExtension.log2(pEntropy), pInfo)}
      .aggregateByKey((0.0, 0f))(
        { case (acc, (pEntropy, pInfo)) => (acc._1 + pEntropy, pInfo) },
        { case ((pEntropy1, pInfo), (pEntropy2, _)) => (pEntropy1 + pEntropy2, pInfo) })
      .mapValues {case (pEntropy, pInfo ) => (pInfo * -pEntropy, pInfo * MathExtension.log2(pInfo))}

//    // Entropy(S_v)
//    // ((j, aj1), (1, ...)), ((j, aj2), (1, ...)), ((j, aj), (2, ...))
//    val mapComputationInputWithEntropy = mapComputationInput
//      .mapValues { case (c, cnt, all) =>
//        (c, cnt, all, (cnt / all.toFloat * MathExtension.log2(cnt / all.toFloat)).toFloat) }
//      .aggregateByKey((0L, 0f))({ case (acc, (_, _, all, part)) => (all, acc._2 + part) }, { case ((all, p1), (_, p2)) => (all, p1 + p2) })
//      .mapValues{ case (all, inverseEntropy) => (all, -inverseEntropy) }

//    println("========== mapComputationInputWithEntropy ==========")
//    println(mapComputationInputWithEntropy.collect.mkString("(", ", ", ")\r\n"))

    // ((j, aj), (all, entropy))
    // all: amount of instances with j = aj
    // entropy->  entropy of subset of instances with j = aj
//    val mapComputationInputWithInfoAndSplitInfo = mapComputationInputWithEntropy
//      .mapValues { case (all, entropy) =>
//        (all / dsLength.toFloat * entropy, all / dsLength.toFloat * MathExtension.log2(all / dsLength.toFloat))
//      }

    // input: ((j, aj), (info(j, aj), splitinfo(j, aj))
    // Gain(a, T) = Dataset entropy - Info(a, T)
    val reduceComputationWithInfoAndSplitInfoForJ = mapComputationInputWithPartialInfoAndSplitInfo
      .map { case ((j, aj), (info, splitInfo)) => (j, (info, splitInfo)) }
      .foldByKey((0, 0))((acc, infoSplitInfo) => (acc._1 + infoSplitInfo._1, acc._2 + infoSplitInfo._2))
      .mapValues{case (info, inverseSplitInfo) => (info, -inverseSplitInfo) }

//    println("====== reduceComputationWithInfoAndSplitInfoForJ ======")
//    println(reduceComputationWithInfoAndSplitInfoForJ.collect.mkString("(", ", ", ")\r\n"))

    val mapComputationWithGainRatio = reduceComputationWithInfoAndSplitInfoForJ
      .mapValues{ case (info, splitInfo) => {
        //println(f"(entropy $entropy - info $info) / splitinfo $splitInfo = ${(entropy - info) / splitInfo}")
        (entropy - info) / splitInfo
      } }

//    println("====== mapComputationWithGainRatio ======")
//    println(mapComputationWithGainRatio.collect.sortBy(_._2).mkString("(", ", ", ")\r\n"))


    val bestAttribute = mapComputationWithGainRatio.filter(!_._2.isInfinity).reduce((res1, res2) => if (res1._2 > res2._2) res1 else res2)

//    println("====== bestAttribute ======")
//    println(bestAttribute)

    // attribute index
    bestAttribute
  }

}

