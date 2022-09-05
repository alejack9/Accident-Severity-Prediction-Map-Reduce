package it.unibo.scalable.ml.dt.sequential

import it.unibo.scalable.Calc
import it.unibo.scalable.ml.dt._
import it.unibo.scalable.ml.dt.sequential.Format.Format
import it.unibo.scalable.ml.dt.sequential.Types._

object Format extends Enumeration {
  type Format = Value
  val Categorical, Continuous = Value
}

object Types {
  type Dataset = Seq[Seq[Float]]
  type Attribute = (Format, Int)
}

object Utility {
  def getMostFrequentTarget(ds: Dataset): Float = ds.map(row => (row.last, 1)).groupBy(_._1).map{case (a, b) => (a, b.length)}.maxBy(_._2)._1
  // def getBranchesFromCondition(ds: Dataset, condition: Condition): Seq[Dataset] = ds.groupBy(sample => condition(sample)).values.toList
}

class C45 {

  def run(ds: Dataset, attributeTypes: Seq[Format]): Tree[Float] = {

    def _run(ds: Dataset, attributes: Seq[Attribute]) : Tree[Float]= {

      if (ds.length == 1) return Leaf(ds.head.last)

      if (attributes.isEmpty)
        return Leaf(Utility.getMostFrequentTarget(ds))

      // check node purity (all samples belongs to the same target)
      if (ds.forall(_.last == ds.head.last)) return Leaf(ds.head.last)

      if (attributes.length == 1) {
        val attrIndex = attributes.head._2
        val attrValues = ds.map(sample => sample(attrIndex)).distinct

        if (attributes.head._1 == Format.Categorical) {

          def cond = CategoricalCondition(attrIndex, attrValues)

          return CondNode(cond, attrValues.map(v => _run(ds.filter(row => row(attrIndex) == v), attributes.patch(0, Nil, 1))))
        }
        else {
          if (attrValues.length == 1) return Leaf(Utility.getMostFrequentTarget(ds))

          // [1,2,3,4] -> [1,2,3] [2,3,4] -> [(1,2), (2,3), (3,4)] => [1.5, 2.5, 3.5]
          val midPoints = attrValues.sorted.init.zip(attrValues.sorted.tail).map{case (a, b) => (a + b) / 2.0 }

          val dsEntropy = Calc.entropy(ds)

          val bestSplitPoint = midPoints.map(midpoint => {
            val partitions = ds.partition(_ (attrIndex) < midpoint)
            val partList = List(partitions._1, partitions._2)
            (midpoint, Calc.infoGainRatio(dsEntropy, partList, ds.length), partList)
          }).maxBy(_._2)

          def cond = ContinuousCondition(attrIndex, bestSplitPoint._1.toFloat)

          return CondNode(cond, bestSplitPoint._3.map(_run(_, attributes)))
        }
      }

      // + 1 attributes are available
      val dsEntropy = Calc.entropy(ds)

      val infoGainRatios: Seq[(Float, Condition[_ <: Float], Seq[Dataset], Int)] = attributes.zipWithIndex.map{case ((format, attrIndex), index) =>
        val attrValues = ds.map(sample => sample(attrIndex)).distinct
        if (attrValues.length == 1) (0.0f, CategoricalCondition(index, Nil), Nil, -1)
        else if (format == Format.Categorical) {
          def cond: Condition[Float] = CategoricalCondition(attrIndex, attrValues)

          val branches = ds.groupBy(sample => sample(attrIndex)).values.toList
          (Calc.infoGainRatio(dsEntropy, branches, ds.length), cond, branches, index)
        } else { // continuous case
          val midPoints = attrValues.sorted.init.zip(attrValues.sorted.tail).map { case (a, b) => (a + b) / 2.0 }

          val bestSplitPoint = midPoints.map(midpoint => {
            val partitions = ds.partition(_ (attrIndex) < midpoint)
            val partList = List(partitions._1, partitions._2)
            (midpoint, Calc.infoGainRatio(dsEntropy, partList, ds.length), partList)
          }).maxBy(_._2)

          def cond: Condition[Float] = ContinuousCondition(attrIndex, bestSplitPoint._1.toFloat)

          (bestSplitPoint._2, cond, bestSplitPoint._3, index)
        }
      }

      if (infoGainRatios.forall(_._1 == 0.0f)) return Leaf(Utility.getMostFrequentTarget(ds))

      val maxGainRatio = infoGainRatios.maxBy(_._1)

      CondNode(
        maxGainRatio._2,
        maxGainRatio._3
          .map(_run(
            _,
            if (attributes(maxGainRatio._4)._1 == Format.Continuous)
              attributes
            else
              attributes.patch(maxGainRatio._4, Nil, 1)
      )))
    }

    _run(ds, attributeTypes.zipWithIndex)
  }
}
