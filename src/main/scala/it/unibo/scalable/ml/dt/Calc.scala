package it.unibo.scalable.ml.dt

import it.unibo.scalable.MathExtension
import it.unibo.scalable.ml.dt.Utils.Types.Dataset

object Calc {

  def entropy[T <: AnyVal](ds: Dataset[Seq[T]]): Float = {
    val targets = ds.map(_.last).distinct

    // - sommatoria per ogni classe p(classe) * log (p)
    -targets.map(target => {
      val p = ds.count(_.last == target) / ds.length.toFloat
      p * MathExtension.log2(p)
    }).sum.toFloat
  }

  def information[T <: AnyVal](subSets: Seq[Dataset[Seq[T]]], dsLength: Long): Float =
  //    we could call information calculating probabilities but we had to run map twice and seq != RDD
    subSets.map(subset => subset.length / dsLength.toFloat * entropy(subset)).sum

  def information[T <: AnyVal](subProbabilities: Seq[Float], subSets: Seq[Dataset[Seq[T]]]): Float =
    subProbabilities.zip(subSets).map { case (p, s) => p * entropy(s) }.sum


  def splitInformation[T <: AnyVal](subsets: Seq[Dataset[Seq[T]]], dsLength: Long): Float =
  //    same as information
    -subsets.map(subset => {
      val p = subset.length / dsLength.toFloat
      p * MathExtension.log2(p)
    }).sum.toFloat

  def splitInformation(subProbabilities: Seq[Float]): Float = -subProbabilities.map(p => p * MathExtension.log2(p)).sum.toFloat

  def infoGainRatio[T <: AnyVal](dsEntropy: Float, subSets: Seq[Dataset[Seq[T]]], dsLength: Long): Float = {
    val subProbs = subSets.map(subset => subset.length / dsLength.toFloat)

    val infoGain = dsEntropy - information(subProbs, subSets)

    infoGain / splitInformation(subProbs)
  }
}
