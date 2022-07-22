package it.unibo.scalable.Tree

import it.unibo.scalable.Format.Format
import it.unibo.scalable.Format

import scala.collection.immutable

object Utility {

  class TreeCond(var cond: Array[Float] => Boolean, var desc: String){

  }

  // NOTE [[METRIC]]
  // is enough analyze only an array of float related to a single feature
  // it isn't necessary to input all dataset
  def getBestSplitting (data: Array[(Array[Float], Float)],
                          features: Array[(String, Format)],
                          metric:Array[(Float, Float)] => Float): (String, (TreeCond, Float)) = {
    // TODO wrap in a map ...
    // TODO parallel here??
    //    var bestSplitting: (Array[T] => Boolean, T)

    // es. [(80.0, 1), (23.0, 1), (1.0, 2), (8.0, 3)] -> (feat value, target)
    def bestForEachFeature: Array[(String, (TreeCond, Float))] =features.zipWithIndex map
    { case ((feature: String, format: Format), id: Int) =>
        println(feature, format.toString, id)
      // [(value for attribute A, target), (value for attribute A, target), (value for attribute A, target)]
        val featDataAndTarget = data.map(sample => (sample._1(id), sample._2))

        if (format equals Format.Ordered) {
          val sortedDistinctValues = featDataAndTarget.map(_._1).distinct.sorted

          if (sortedDistinctValues.length > 1) {
            //[55, 20, 11] : zip [55, 10] and [10, 20] => [(55, 10), (10, 20)]
             val toRet = (feature,
              sortedDistinctValues.init.zip(sortedDistinctValues.tail)
              .map((seqValues:(Float, Float)) => (seqValues._1 + seqValues._2) / 2)
                .map(
                  (average: Float) =>
                    ( new TreeCond(
                      (feats: Array[Float]) => feats(id) > average, s"$feature > $average" ),
                    getMetricValue((Array((0.1f,0.1f), (0.1f,0.1f)), Array((0.1f,0.1f), (0.1f,0.1f))), metric))
                ).maxBy(_._2))

            println(toRet._1 + " best splitting criteria" + toRet._2._1.desc)
            toRet

          }
          else {
            // metric -> minimum
            def cond =  new TreeCond(
              (feats: Array[Float]) => feats(id) == sortedDistinctValues(0), s"$feature == ${sortedDistinctValues(0)}" )

            val toRet = (feature, (cond, 0f ))

            println(toRet._1 + " best splitting criteria" + toRet._2._1.desc)
            toRet
          }
        }
        else { // is not ordered
          val subsets = getCategoriesSubsets(featDataAndTarget.map(_._1).distinct)

          def cond(subset: Set[Float]) = new TreeCond(
            (feats: Array[Float]) => subset.contains(feats(id)), s"$feature contained in { ${subset.mkString(",")} }" )

          // (nome feature, (best conditions splitting , best metric value))
          val toRet = (feature, subsets.map(subset => (
            cond(subset),
            getMetricValue(featDataAndTarget.partition((el: (Float, Float)) => subset.contains(el._1)), metric))
          ).maxBy(_._2))

          println(toRet._1 + " best splitting criteria" + toRet._2._1.desc)
          println()

          toRet
        }

      //      [(nome feature1, (bestcondition, bestmetric value)), (nome feature2, (best condition, bestmetric value))]
    }

    bestForEachFeature.maxBy(_._2._2)
    //  [(featureName, (fun, metricValue)) )]


  }

    def getCategoriesSubsets(categories: Array[Float]): Array[Set[Float]] = {
      val subsets = categories.toSet.subsets.toArray.tail // all but first (empty list) and, if the list.length is >=3 also the last(all the list)

      // subsets hasn't the empty set
      if (subsets.length > 1) {
        subsets.init
      } else {
        subsets
      }
    }

    def getMetricValue(branches: (Array[(Float, Float)], Array[(Float, Float)]), metric: Array[(Float, Float)] => Float): Float ={
      0.1f
//      throw new NotImplementedError()
    }

    def getDominantTarget(data: Array[(Array[Float], Float)]): Float = {
      data.groupBy(_._2).map(elems => (elems._1, elems._2.length)).maxBy(_._2)._1
    }

    def isPure(data: Array[(Array[Float], Float)]): Boolean = {
      data.forall(_._2 == data.head._2)
    }

    def splitData(data: Array[(Array[Float], Float)], treeCond: TreeCond):
      (Array[(Array[Float], Float)],
      Array[(Array[Float], Float)]) = {

      data.partition(sample => treeCond.cond(sample._1))

    }
}
