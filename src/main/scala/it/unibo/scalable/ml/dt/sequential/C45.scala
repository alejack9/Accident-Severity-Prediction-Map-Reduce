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

class C45 {

  def run(ds: Dataset, attributeTypes: Seq[Format]): Tree[Float] = {
    def _run(ds: Dataset, attributes: Seq[Attribute]) : Tree[Float]= {
      if (attributes.isEmpty)
        return Leaf(ds.map(row => (row.last, 1)).groupBy(_._1).map{case (a, b) => (a, b.length)}.maxBy(_._2)._1)


      if (attributes.length == 1) {
        val attrIndex = attributes.head._2
        val values = ds.map(sample => sample(attrIndex)).distinct

        if (attributes.head._1 == Format.Categorical) {

          // 2, 3, 0, 4

          def cond: Seq[Float] => Int = sample => values.indexOf(sample(attrIndex))

          CondNode(cond, values.map(v => _run(ds.filter(row => row(attrIndex) == v), attributes.patch(0, Nil, 1))))
        }
        else {
          // [1,2,3,4] -> [1,2,3] [2,3,4] -> [(1,2), (2,3), (3,4)] => [1.5, 2.5, 3.5]
          val midPoints = values.sorted.init.zip(values.sorted.tail).map{case (a, b) => (a + b) / 2.0 }

          val dsEntropy = Calc.entropy(ds)

          // TODO
          midPoints.map(midpoint => (midpoint, Calc.infoGainRatio(dsEntropy, branches, ds.length)))

        }
      }


      Leaf(-1)
    }
    print(ds.head.init.indices.toList)
    _run(ds, attributeTypes.zipWithIndex)
  }
}
