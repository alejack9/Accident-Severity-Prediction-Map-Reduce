package it.unibo.scalable.ml.dt.par

import it.unibo.scalable.ml.dt.Utils.Format
import it.unibo.scalable.ml.dt.Utils.Format.Format
import it.unibo.scalable.ml.dt.Utils.Types.{Attribute, Dataset}
import it.unibo.scalable.ml.dt.{Tree, _}

import scala.util.{Failure, Success, Try}

class C45() extends C45Alg {

  private def bestContinuousSplitPoint[T <: Seq[Float]](ds: Dataset[T], dsEntropy: Float, attrValues: Seq[Float], attrIndex: Int)
  : (ContinuousCondition[Float], Float, Seq[Dataset[T]]) = {
    // [1,2,3,4] -> [1,2,3] [2,3,4] -> [(1,2), (2,3), (3,4)] => [1.5, 2.5, 3.5]
    val midPoints = attrValues.sorted.init.zip(attrValues.sorted.tail).map { case (a, b) => (a + b) / 2.0 }

    val bestSplitPoint = midPoints.map(midpoint => {
      val partitions = ds.partition(row => row(attrIndex) < midpoint)
      val partList = List(partitions._1, partitions._2)
      (midpoint, Calc.infoGainRatio(dsEntropy, partList, ds.length), partList)
    }).maxBy(_._2)

    (ContinuousCondition(attrIndex, bestSplitPoint._1.toFloat), bestSplitPoint._2, bestSplitPoint._3)
  }

  // the last value of each sample represents the class target
  override def train[T <: Seq[Float]](ds: Dataset[T], attributeTypes: Seq[Format]): Tree[Float] = {

    def _train(ds: Dataset[T], attributes: Seq[Attribute], depth: Int): Try[Tree[Float]] = try {
      //        println("|ds| : " + ds.length)
      //        println("|attrs| : " + attributes.length)
      //        println("Depth: " + depth)

      if (ds.length == 1) return Success(Leaf(ds.head.last))

      if (attributes.isEmpty) return Success(LeafFactory.get(ds))

      // check node purity (all samples belongs to the same target)
      if (ds.forall(_.last == ds.head.last)) return Success(Leaf(ds.head.last))

      // only an attribute left
      if (attributes.length == 1) {
        val attrIndex = attributes.head._2
        val attrValues = ds.map(sample => sample(attrIndex)).distinct

        if (attributes.head._1 == Format.Categorical) {
          return Success(CondNode(
            CategoricalCondition(attrIndex, attrValues),
            attrValues.map(v => {
              _train(ds.filter(row => row(attrIndex) == v), attributes.patch(0, Nil, 1), depth + 1) match {
                case Success(value) => value
                case Failure(_) =>
                  //                  println(f"Stackoverflow error, leaf created at depth ${depth}")
                  LeafFactory.get(ds)
              }
            })
          ))
        } else {
          if (attrValues.length == 1) return Success(LeafFactory.get(ds))

          val (cond, _, subDss) = bestContinuousSplitPoint(ds, Calc.entropy(ds), attrValues, attrIndex)
          return Success(CondNode(cond, subDss.map(_train(_, attributes, depth + 1) match {
            case Success(value) => value
            case Failure(_) =>
              // Stackoverflow error, leaf created instead
              LeafFactory.get(ds)
          })))
        }
      }

      // + 1 attributes are available
      val dsEntropy = Calc.entropy(ds)

      val infoGainRatios: Seq[(Float, Condition[_ <: Float], Seq[Dataset[T]], Int)] = attributes.zipWithIndex.map { case ((format, attrIndex), index) =>
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

      if (infoGainRatios.forall(_._1 == 0.0f)) return Success(LeafFactory.get(ds))

      val maxGainRatio = infoGainRatios.maxBy(_._1)

      Success(CondNode(
        maxGainRatio._2,
        maxGainRatio._3
          .map(_train(
            _,
            if (attributes(maxGainRatio._4)._1 == Format.Continuous)
              attributes
            else
              attributes.patch(maxGainRatio._4, Nil, 1),
            depth + 1
          ) match {
            case Success(value) => value
            case Failure(_) =>
              // Stackoverflow error, leaf created instead
              LeafFactory.get(ds)
          })))
    } catch {
      case e: StackOverflowError =>
        Failure(e)
      case e: OutOfMemoryError =>
        Failure(e)
    }

    _train(ds, attributeTypes.zipWithIndex, 0) match {
      case Success(toRet) => toRet
      case Failure(e) => throw e
    }
  }
}
