package it.unibo.scalable

import it.unibo.scalable.ml.dt.sequential.Types._

object Calc {
  def entropy(ds: Dataset): Float = {
    val targets = ds.map(_.last).distinct

    // - sommatoria per ogni classe p(classe) * log (p)
    - targets.map(target => {
      val p = ds.count(_.last == target) / ds.length
      p * math.log(p)
    }).sum.toFloat
  }

  def information(subSets: Seq[Dataset], dsLength: Long): Float =
//    we could call information calculating probabilities but we had to run map twice and seq != RDD
    - subSets.map(subset => subset.length / dsLength.toFloat * entropy(subset)).sum

  def information(subProbabilities: Seq[Float], subSets: Seq[Dataset]): Float =
    - subProbabilities.zip(subSets).map{case (p, s) => p * entropy((s))}.sum


  def splitInformation(subsets: Seq[Dataset], dsLength: Long): Float =
//    same as information
    - subsets.map ( subset => {
      val p = subset.length / dsLength.toFloat
      p * math.log(p)
    } ).sum.toFloat

  def splitInformation(subProbabilities: Seq[Float]): Float = - subProbabilities.map(p => p * math.log(p)).sum.toFloat

  def infoGainRatio(dsEntropy: Float, subSets: Seq[Dataset], dsLength: Long) {
    val subProbs = subSets.map(subset => subset.length / dsLength.toFloat)

    val infoGain = dsEntropy - information(subProbs, subSets)

    infoGain / splitInformation(subProbs)
  }
}
