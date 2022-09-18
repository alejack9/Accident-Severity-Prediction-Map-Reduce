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
  type Dataset[T <: Seq[Float]] = Seq[T]
  type Attribute = (Format, Int)
}

class C45 {
  private def bestContinuousSplitPoint[T <: Seq[Float]](ds: Dataset[T], dsEntropy: Float, attrValues: Seq[Float], attrIndex: Int)
    : (ContinuousCondition[Float], Float, Seq[Dataset[T]]) = {
      // [1,2,3,4] -> [1,2,3] [2,3,4] -> [(1,2), (2,3), (3,4)] => [1.5, 2.5, 3.5]
      val midPoints = attrValues.sorted.init.zip(attrValues.sorted.tail).map{case (a, b) => (a + b) / 2.0 }

      val bestSplitPoint = midPoints.map(midpoint => {
        val partitions = ds.partition(row => row(attrIndex) < midpoint)
        val partList = List(partitions._1, partitions._2)
        (midpoint, Calc.infoGainRatio(dsEntropy, partList, ds.length), partList)
      }).maxBy(_._2)

      (ContinuousCondition(attrIndex, bestSplitPoint._1.toFloat), bestSplitPoint._2, bestSplitPoint._3)
  }

  // the last value of each sample represents the class target
  def run[T <: Seq[Float]](ds: Dataset[T], attributeTypes: Seq[Format]): Tree[Float] = {

    def _run(ds: Dataset[T], attributes: Seq[Attribute]) : Tree[Float]= {

      if (ds.length == 1) return Leaf(ds.head.last)

      if (attributes.isEmpty) return LeafFactory.get(ds)

      // check node purity (all samples belongs to the same target)
      if (ds.forall(_.last == ds.head.last)) return Leaf(ds.head.last)

      // only an attribute left
      if (attributes.length == 1) {
        val attrIndex = attributes.head._2
        val attrValues = ds.map(sample => sample(attrIndex)).distinct

        if (attributes.head._1 == Format.Categorical)
          return CondNode(
            CategoricalCondition(attrIndex, attrValues),
            attrValues.map(v => _run(ds.filter(row => row(attrIndex) == v), attributes.patch(0, Nil, 1)))
          )
        else {
          if (attrValues.length == 1) return LeafFactory.get(ds)

          val (cond, _, subDss) = bestContinuousSplitPoint(ds, Calc.entropy(ds), attrValues, attrIndex)
          return CondNode(cond, subDss.map(_run(_, attributes)))
        }
      }

      // + 1 attributes are available
      val dsEntropy = Calc.entropy(ds)

      val infoGainRatios: Seq[(Float, Condition[_ <: Float], Seq[Dataset[T]], Int)] = attributes.zipWithIndex.map{case ((format, attrIndex), index) =>
        val attrValues = ds.map(sample => sample(attrIndex)).distinct

        if (attrValues.length == 1) (0.0f, CategoricalCondition(index, Nil), Nil, -1)
        else if (format == Format.Categorical) {
          def cond: Condition[Float] = CategoricalCondition(attrIndex, attrValues)

          val branches = ds.groupBy(sample => sample(attrIndex)).values.toList
          (Calc.infoGainRatio(dsEntropy, branches, ds.length), cond, branches, index)
        } else { // continuous case

          val (cond, infoGainRatio, subDss) = bestContinuousSplitPoint(ds, dsEntropy, attrValues, attrIndex)
          (infoGainRatio, cond, subDss, index)
        }
      }

      if (infoGainRatios.forall(_._1 == 0.0f)) return LeafFactory.get(ds)

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
