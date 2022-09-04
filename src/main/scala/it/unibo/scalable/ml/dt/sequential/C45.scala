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
  def getMostFrequentTarget(ds: Dataset) = ds.map(row => (row.last, 1)).groupBy(_._1).map{case (a, b) => (a, b.length)}.maxBy(_._2)._1
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

          def cond: Condition = new Condition(sample => attrValues.indexOf(sample(attrIndex)), s"feat $attrIndex $attrValues")

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

          def cond: Condition = new Condition(sample => if (sample(attrIndex) < bestSplitPoint._1) 0 else 1, s"feat $attrIndex < ${bestSplitPoint._1}")

          return CondNode(cond, bestSplitPoint._3.map(_run(_, attributes)))
        }
      }

      // + 1 attributes are available
      val dsEntropy = Calc.entropy(ds)

      val infoGainRatios: Seq[(Float, Condition)] = attributes.map{case (format, attrIndex) =>
        val attrValues = ds.map(sample => sample(attrIndex)).distinct
        if (attrValues.length == 1) (0.0f, new Condition(_ => -1))
        else if (format == Format.Categorical) {
          def cond: Condition = new Condition(sample => attrValues.indexOf(sample(attrIndex)), s"feat $attrIndex $attrValues")

          (Calc.infoGainRatio(dsEntropy, ds.groupBy(sample => sample(attrIndex)).values.toList, ds.length), cond)
        } else {
          val midPoints = attrValues.sorted.init.zip(attrValues.sorted.tail).map { case (a, b) => (a + b) / 2.0 }

          val bestSplitPoint = midPoints.map(midpoint => {
            val partitions = ds.partition(_ (attrIndex) < midpoint)
            val partList = List(partitions._1, partitions._2)
            (midpoint, Calc.infoGainRatio(dsEntropy, partList, ds.length), partList)
          }).maxBy(_._2)

          def cond: Condition = new Condition(sample => if (sample(attrIndex) < bestSplitPoint._1) 0 else 1, s"feat $attrIndex < ${bestSplitPoint._1}")

          (bestSplitPoint._2, cond)
        }
      }

      println(infoGainRatios)

      // TODO: take best infoGainRatio and make a node with that
      ???


      throw new NotImplementedError()
    }

    _run(ds, attributeTypes.zipWithIndex)
  }
}
