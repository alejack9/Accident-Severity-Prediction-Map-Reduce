package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.MathExtension
import it.unibo.scalable.ml.dt.spark.Types.Dataset
import org.apache.spark.storage.StorageLevel
// import org.apache.spark.Partitioner
import org.apache.spark.rdd.RDD

import scala.collection.immutable.HashMap

object Types {
  type Dataset = RDD[Seq[Float]]
}

// The chosen data structure that represents the DT is a hashmap where
// - key: list of tuples (feat index, feat value) representing the path for the node
// - value: node object. I can be a Link(index of the splitting feature) or Leaf(target class)
class C45 {

  def train(D: Dataset): Map[List[(Int, Float)], Node] = {

    def _train(dataset: Dataset, path: List[(Int, Float)], treeTable: Map[List[(Int, Float)], Node], features: Seq[Int] ): Map[List[(Int, Float)], Node] = {

      val cached = dataset.persist(StorageLevel.MEMORY_AND_DISK)

      if (features.isEmpty)
        return treeTable + (path -> Leaf(getClass(cached)))

      // get the best attribute index with the related gain ratio
      val bestAttrIndex = getBestAttribute(cached, features)

      if (bestAttrIndex._2 == 0.0 // If the best chosen gain ratio is 0.0 then there are no more splits that carry info
        || bestAttrIndex._2.isNaN) // NaN means that the subset has 1 sample only
        return treeTable + (path -> Leaf(getClass(cached)))

      // get all the distinct values in the dataset for the best chosen feature
      val bestAttrValues = cached.map(_ (bestAttrIndex._1)).distinct.collect

      // for each possible value, create a subnode and update the tree table
      bestAttrValues
        .map(value => {
          val current = (bestAttrIndex._1, value)

          if (path.contains(current)) // if this couple (feature, value) already exists in the node path
            treeTable + ((path :+ current) -> Leaf(getClass(cached)))
          else
            _train(
              cached.filter(_ (bestAttrIndex._1) == value), // the subset
              path :+ current, // the path of the subset node
              treeTable + (path -> Link(bestAttrIndex._1)), // the tree table updated with the current node as a link
              features.patch(bestAttrIndex._1, Nil, 1),
            )
        })
        .reduce(_ ++ _)
    }

    _train(D, List.empty, HashMap.empty, D.first.indices)
  }

  // Entropy(D) = - sum(p(D, c) * log2(p(D,c)) for each class c
  def calcEntropy(in: RDD[((Int, Float), (Float, Long, Long))], dsLength: Long): Float = {

    val firstJ = in.first._1._1

    // in questo pezzo di codice facciamo la somma (il reduce) per ogni chiave ma ci basterebbe prendere le chiavi che hanno la stessa J e lavorare solo su quei record
    //                         j ,  a_j       c     cnt   all
    - in
      .filter { case ((j, _), _) => j == firstJ }
      .map { case ((_, _), (c, cnt, _)) => (c, cnt) }
      .reduceByKey(_ + _)
      .map(v => {
        val p = v._2 / dsLength.toFloat
        p * MathExtension.log2(p)
      }).sum.toFloat
  }

  // take the class with most occurrences
  def getClass(D: Dataset): Float = D.map(_.last).countByValue().maxBy(_._2)._1

  def getBestAttribute(D: Dataset, features: Seq[Int]): (Int, Double) = {
    val dsLength = D.count()

    // 1st map-reduce step: DATA PREPARATION
    // out ((j, a_j), (class, count))
    val dataPreparationRes: RDD[((Int, Float), (Float, Long))] = D
      // Map attribute step
//      .zipWithIndex // attach the row id
      .flatMap(row =>
        row.init.indices.filter(i => features.contains(i)).map(i => ((i, row(i), row.last), 1L))
      )
      // Reduce attribute step
//      .map { case ((j, aj), c) => ((j, aj, c), 1L) }
//      .filter {case ((j, _, _), _) => features.contains(j) }
      .reduceByKey(_ + _)
      .map { case ((j, aj, c), cnt) => ((j, aj), (c, cnt)) }
//      .partitionBy(Partitioner.defaultPartitioner() 10)
      .persist(StorageLevel.MEMORY_AND_DISK)

    // 2nd map-reduce step: ATTRIBUTE SELECTION
    // reduce population
    val reducePopulationRes: RDD[((Int, Float), Long)] = dataPreparationRes
      .aggregateByKey(0L)({ case (acc, (_, cnt)) => acc + cnt }, _ + _)

    // add all (number of samples for with j == aj)
    val mapComputationInput: RDD[((Int, Float), (Float, Long, Long))] = dataPreparationRes
      .join(reducePopulationRes)
      .mapValues { case ((c, cnt), all) => (c, cnt, all) }
      .persist(StorageLevel.MEMORY_AND_DISK)


    // calc general entropy of the set D, useful in the calculation of the gain ratio
    val entropy = calcEntropy(mapComputationInput, dsLength)

    val mapComputationInputWithPartialInfoAndSplitInfoForJ: RDD[(Int, (Double, Double))] = mapComputationInput //   p for entropy       p for info
      .map { case ((j, aj), (_, cnt, all)) => ((j, aj), (cnt / all.toFloat * MathExtension.log2(cnt / all.toFloat), all / dsLength.toFloat)) }
      .aggregateByKey((0.0, 0f))(
        { case (acc, (pEntropy, pInfo)) => (acc._1 + pEntropy, pInfo) },
        { case ((pEntropy1, pInfo), (pEntropy2, _)) => (pEntropy1 + pEntropy2, pInfo) })
      .map { case ((j, _), (pEntropy, pInfo)) => (j, (pInfo * -pEntropy, pInfo * MathExtension.log2(pInfo))) }

    // input: ((j, aj), (info(j, aj), splitinfo(j, aj))
    // Gain(a, T) = Dataset entropy - Info(a, T)
    val mapComputationWithGainRatio = mapComputationInputWithPartialInfoAndSplitInfoForJ
      .foldByKey((0, 0))((acc, infoSplitInfo) => (acc._1 + infoSplitInfo._1, acc._2 + infoSplitInfo._2))
      .mapValues { case (info, inverseSplitInfo) => (entropy - info) / -inverseSplitInfo }

//    val mapComputationWithGainRatio = reduceComputationWithInfoAndSplitInfoForJ
//      .mapValues { case (info, splitInfo) => (entropy - info) / splitInfo }

    val bestAttribute = mapComputationWithGainRatio
      .filter(!_._2.isInfinity)
      .reduce((res1, res2) =>
        if (res1._2 > res2._2) res1
        else res2
      )

    // attribute index
    bestAttribute
  }

}

