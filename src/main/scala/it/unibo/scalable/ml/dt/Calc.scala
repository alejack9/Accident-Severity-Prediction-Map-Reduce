package it.unibo.scalable.ml.dt

import it.unibo.scalable.ml.dt.Utils.Types.Dataset

object Calc {

  def entropy[T <: Seq[Float]](ds: Dataset[T]): Float = {
    val targets = ds.map(_.last).distinct

    // - sommatoria per ogni classe p(classe) * log (p)
    -targets.map(target => {
      val p = ds.count(_.last == target) / ds.length.toFloat
      p * math.log(p)
    }).sum.toFloat
  }

  def information[T <: Seq[Float]](subSets: Seq[Dataset[T]], dsLength: Long): Float =
  //    we could call information calculating probabilities but we had to run map twice and seq != RDD
    -subSets.map(subset => subset.length / dsLength.toFloat * entropy(subset)).sum

  def information[T <: Seq[Float]](subProbabilities: Seq[Float], subSets: Seq[Dataset[T]]): Float =
    -subProbabilities.zip(subSets).map { case (p, s) => p * entropy(s) }.sum


  def splitInformation[T <: Seq[Float]](subsets: Seq[Dataset[T]], dsLength: Long): Float =
  //    same as information
    -subsets.map(subset => {
      val p = subset.length / dsLength.toFloat
      p * math.log(p)
    }).sum.toFloat

  def splitInformation(subProbabilities: Seq[Float]): Float = -subProbabilities.map(p => p * math.log(p)).sum.toFloat

  def infoGainRatio[T <: Seq[Float]](dsEntropy: Float, subSets: Seq[Dataset[T]], dsLength: Long): Float = {
    val subProbs = subSets.map(subset => subset.length / dsLength.toFloat)

    val infoGain = dsEntropy - information(subProbs, subSets)

    infoGain / splitInformation(subProbs)
  }
}
