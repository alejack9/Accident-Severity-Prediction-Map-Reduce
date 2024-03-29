package it.unibo.scalable.ml.dt.base

import it.unibo.scalable.ml.dt.Utils.{Format, GenSeqSort}
import it.unibo.scalable.ml.dt.Utils.Format.Format
import it.unibo.scalable.ml.dt.Utils.Types.{Attribute, Dataset}
import it.unibo.scalable.ml.dt._

import scala.collection.GenSeq
import scala.util.{Failure, Success, Try}

class C45[C <: AnyVal : Fractional] {

  private def bestContinuousSplitPoint[T <: Seq[C]](ds: Dataset[T], dsEntropy: Float, attrValues: GenSeq[C], attrIndex: Int)
  : (ContinuousCondition[C], Float, Seq[Dataset[T]]) = {
    // https://stackoverflow.com/a/23847107 : best way to sort the array if par -> convert parseq to seq an then back to parseq
    // [1,2,3,4] -> [1,2,3] [2,3,4] -> [(1,2), (2,3), (3,4)] => [1.5, 2.5, 3.5]

    val attrValuesSorted = attrValues.sort()
    val midPoints : GenSeq[C] = attrValuesSorted.init.zip(attrValuesSorted.tail).map { case (a, b) =>
      implicitly[Fractional[C]].div(implicitly[Fractional[C]].plus(a, b), implicitly[Fractional[C]].fromInt(2)) }

    val bestSplitPoint = midPoints.map(midpoint => {
      val partitions = ds.partition(row => implicitly[Fractional[C]].lt(row(attrIndex), midpoint))
      val partList = List(partitions._1, partitions._2)
      (midpoint, Calc.infoGainRatio(dsEntropy, partList, ds.length), partList)
    }).maxBy(_._2)

    (ContinuousCondition(attrIndex, bestSplitPoint._1), bestSplitPoint._2, bestSplitPoint._3)
  }

  // the last value of each sample represents the class target
  def train[T <: Seq[C]](ds: Dataset[T], attributeTypes: Seq[Format]): Tree[C] = {

    def _train(ds: Dataset[T], attributes: Seq[Attribute], depth: Int): Try[Tree[C]] = try {
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
              _train(ds.filter(row => row(attrIndex) == v), attributes.patch(0, Nil, 1), depth + 1) match {
                case Success(value) => value
                case Failure(_) =>
                  // Stack overflow error, leaf created instead
                  LeafFactory.get[C, T](ds)
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
              LeafFactory.get[C, T](ds)
          })) )
        }
      }

      // + 1 attributes are available
      val dsEntropy = Calc.entropy(ds)

      val infoGainRatios: Seq[(Float, Condition[_ <: C], Seq[Dataset[T]], Int)] = attributes.zipWithIndex
        .map { case ((format, attrIndex), index) =>
          val attrValues = ds.map(sample => sample(attrIndex)).distinct.sort()

          if (attrValues.length == 1) (0.0f, CategoricalCondition(index, Nil), Nil, -1)
          else if (format == Format.Categorical) {
            def cond: Condition[C] = CategoricalCondition(attrIndex, attrValues)

            // [feat value, [samples]]
            val branches = ds.groupBy(sample => sample(attrIndex)).toList.sortBy(_._1).map(_._2)
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
              // Stack overflow error, leaf created instead
              LeafFactory.get[C, T](ds)
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
