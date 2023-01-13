package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.MathExtension
import it.unibo.scalable.ml.dt.spark.Types.Dataset
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.SparkContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.rdd.RDD

import scala.collection.immutable.HashMap

object Types {
  type Dataset = RDD[Seq[Float]]
}

// The chosen data structure that represents the DT is a hashmap where
// - key: list of tuples (feat index, feat value) representing the path for the node
// - value: node object. I can be a Link(index of the splitting feature) or Leaf(target class)
class C45 {
  private val sc: SparkContext = ContextFactory.getContext()

  def train(D: Dataset): Map[List[(Int, Float)], Node] = {

    def _train(dataset: Dataset, path: List[(Int, Float)], treeTable: Map[List[(Int, Float)], Node], features: Broadcast[Seq[Int]]): Map[List[(Int, Float)], Node] = {

      val cached = dataset.persist(StorageLevel.MEMORY_AND_DISK)

      // get the best attribute index with the related gain ratio
      val bestAttrIndex = getBestAttribute(cached, features)

      if (bestAttrIndex._2 == 0.0 // If the best chosen gain ratio is 0.0 then there are no more splits that carry info
        || bestAttrIndex._2.isNaN
        || bestAttrIndex._2.isInfinity) // NaN means that the subset has 1 sample only
          return treeTable + (path -> Leaf(getClass(cached)))

      // get all the distinct values in the dataset for the best chosen feature
      val bestAttrValues = cached.map(_ (bestAttrIndex._1)).distinct.collect

      bestAttrValues
        .foldLeft(treeTable) { (acc, value) =>
          val current = (bestAttrIndex._1, value)
          if (path.contains(current)) {
            acc + ((path :+ current) -> Leaf(getClass(cached)))
          } else {
            val updatedTreeTable = acc + (path -> Link(bestAttrIndex._1))
            _train(
              cached.filter(_ (bestAttrIndex._1) == value),
              path :+ current,
              updatedTreeTable,
              sc.broadcast(features.value.filter(_ != bestAttrIndex._1))
            )
          }
        }
    }

    _train(D, List.empty, HashMap.empty, sc.broadcast(D.first.indices))
  }

  // Entropy(D) = - sum(p(D, c) * log2(p(D,c)) for each class c
//  def calcEntropy(in: RDD[((Int, Float), ((Float, Long), Long))], dsLength: Broadcast[Long]): Float = {
//
//    val firstJ = in.first._1._1
//
//    // in questo pezzo di codice facciamo la somma (il reduce) per ogni chiave ma ci basterebbe prendere le chiavi che hanno la stessa J e lavorare solo su quei record
//    //                         j ,  a_j       c     cnt   all
//    -in
//      .filter { case ((j, _), _) => j == firstJ }
//      .map { case ((_, _), ((c, cnt), _)) => (c, cnt) }
//      .reduceByKey(_ + _)
//      .aggregate(0f)({ case (acc, (_, v)) =>
//        val p = v / dsLength.value.toFloat
//        acc + p * MathExtension.log2(p)
//      }, _ + _)
//  }

  def calcEntropy(in: Dataset, dsLength: Broadcast[Long]): Float = {
    // Count the number of occurrences of each class
    val classCounts = in.map(_.last).countByValue()

    // Compute the entropy
    classCounts.values.map { count =>
      val p = count.toFloat / dsLength.value
      -p * MathExtension.log2(p)
    }.sum
  }

  // take the class with most occurrences
  def getClass(D: Dataset): Float = D.map(_.last).countByValue().maxBy(_._2)._1

  def getBestAttribute(D: Dataset, features: Broadcast[Seq[Int]]): (Int, Float) = {
    val dsLength = sc.broadcast(D.count())

    val attribute_table: RDD[((Int, Float), Float)] = D
      .flatMap(row =>
        row.init.indices.map(i => ((i, row(i)), row.last))
      ).filter({ case ((j, _), _) => features.value.contains(j) })

    val count_table = attribute_table
      .map { case ((j, aj), c) => ((j, aj, c), 1L) }
      .reduceByKey(_ + _)
      .map { case ((j, aj, c), cnt) => ((j, aj), (c, cnt)) }
      .persist(StorageLevel.MEMORY_AND_DISK)

    val population = count_table
      .aggregateByKey(0L)({ case (acc, (_, cnt)) => acc + cnt }, _ + _)

    val computationInput = count_table
      .join(population)

    // calc general entropy of the set D, useful in the calculation of the gain ratio
//    val entropy = sc.broadcast(calcEntropy(computationInput, dsLength))
    val entropy = sc.broadcast(calcEntropy(D, dsLength))

    val computation = computationInput
      .mapValues({ case ((_, cnt), all) =>
        val p = cnt / all.toFloat
        val entropyjAjC = -p * MathExtension.log2(p) // partial entropy for j aj dataset and class c

        val infojAj = all / dsLength.value.toFloat

        val splitInfojAj = -infojAj * MathExtension.log2(infojAj)

        (entropyjAjC, infojAj, splitInfojAj)
      })
      .reduceByKey { case ((entropy1, info, split), (entropy2, _, _)) =>
        (entropy1 + entropy2, info, split)
      }
      .map { case ((j, _), v) => (j, v) }
      .aggregateByKey((0f, 0f))(
        { case (acc, (entropy1, info1, split1)) => (acc._1 + info1 * entropy1, acc._2 + split1) },
        { case (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2) })
      .mapValues { case (info, splitInfo) => (entropy.value - info) / splitInfo }

    val aBest = computation
      .reduce((res1, res2) =>
        if (res1._2 > res2._2) res1
        else res2
      )

    aBest
  }

}
