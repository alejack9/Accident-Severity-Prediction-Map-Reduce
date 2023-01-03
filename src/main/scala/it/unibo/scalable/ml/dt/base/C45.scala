package it.unibo.scalable.ml.dt.base

import it.unibo.scalable.ml.dt.Utils.{Format, GenSeqSort}
import it.unibo.scalable.ml.dt.Utils.Format.Format
import it.unibo.scalable.ml.dt.Utils.Types.{Attribute, Dataset}
import it.unibo.scalable.ml.dt._

import scala.collection.GenSeq
import scala.util.{Failure, Success, Try}

class C45() extends C45Alg {

  private def bestContinuousSplitPoint[T <: Seq[Float]](ds: Dataset[T], dsEntropy: Float, attrValues: GenSeq[Float], attrIndex: Int)
  : (ContinuousCondition[Float], Float, Seq[Dataset[T]]) = {
    // https://stackoverflow.com/a/23847107 : best way to sort the array if par -> convert parseq to seq an then back to parseq
    // [1,2,3,4] -> [1,2,3] [2,3,4] -> [(1,2), (2,3), (3,4)] => [1.5, 2.5, 3.5]

    val attrValuesSorted = attrValues.sort()
    val midPoints = attrValuesSorted.init.zip(attrValuesSorted.tail).map { case (a, b) => (a + b) / 2.0f }

    val bestSplitPoint = midPoints.map(midpoint => {
      val partitions = ds.partition(row => row(attrIndex) < midpoint)
      val partList = List(partitions._1, partitions._2)
      (midpoint, Calc.infoGainRatio(dsEntropy, partList, ds.length), partList)
    }).maxBy(_._2)

    (ContinuousCondition(attrIndex, bestSplitPoint._1), bestSplitPoint._2, bestSplitPoint._3)
  }

  // the last value of each sample represents the class target
  override def train[T <: Seq[Float]](ds: Dataset[T], attributeTypes: Seq[Format]): Tree[Float] = {

    def _train(ds: Dataset[T], attributes: Seq[Attribute], depth: Int): Try[Tree[Float]] = try {

      if (attributes.isEmpty
        || ds.forall(_.last == ds.head.last)) return Success(LeafFactory.get(ds))

      // only an attribute left
      if (attributes.length == 1) {
        // return tree with single node

        val attrIndex = attributes.head._2
        val attrValues = ds.map(sample => sample(attrIndex)).distinct.sort()

        if (attributes.head._1 == Format.Categorical) {
          return Success(CondNode(
            CategoricalCondition(attrIndex, attrValues),
            attrValues.map(v => {
              _train(ds.filter(row => row(attrIndex) == v), attributes, depth + 1) match {
                case Success(value) => value
                case Failure(_) =>
                  // Stack overflow error, leaf created instead
                  LeafFactory.get(ds)
              }
            })
          ))
        }
        else {
          // just because bestContinuousSplitPoint is heavy
          if (attrValues.length == 1) return Success(LeafFactory.get(ds))

          val (cond, _, subDss) = bestContinuousSplitPoint(ds, Calc.entropy(ds), attrValues, attrIndex)

          return Success( CondNode(cond, subDss.map(_train(_, attributes, depth + 1) match {
            case Success(value) => value
            case Failure(_) =>
              // Stack overflow error, leaf created instead
              LeafFactory.get(ds)
          })) )
        }
      }

      // + 1 attributes are available

      val dsEntropy = Calc.entropy(ds)

      val infoGainRatios: Seq[(Float, Condition[_ <: Float], Seq[Dataset[T]], Int)] = attributes.zipWithIndex
        .map { case ((format, attrIndex), index) =>
          val attrValues = ds.map(sample => sample(attrIndex)).distinct.sort()

          if (attrValues.length == 1) (0.0f, CategoricalCondition(index, Nil), Nil, -1)
          else if (format == Format.Categorical) {
            def cond: Condition[Float] = CategoricalCondition(attrIndex, attrValues)

            // [feat value, [samples]]
            val branches = ds.groupBy(sample => sample(attrIndex)).toList.sortBy(_._1).map(_._2)
            (Calc.infoGainRatio(dsEntropy, branches, ds.length), cond, branches, index)
          } else { // continuous case
            val (cond, infoGainRatio, subDss) = bestContinuousSplitPoint(ds, dsEntropy, attrValues, attrIndex)
            (infoGainRatio, cond, subDss, index)
          }
        }

//      println("============== info gain ratios ")
//      println(infoGainRatios.map(t => (t._4, t._1)).sortBy(_._2).mkString(","))

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
              attributes,
            depth + 1
          ) match {
            case Success(value) => value
            case Failure(_) =>
              // Stack overflow error, leaf created instead
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
