package it.unibo.scalable.ml.dt

import scala.collection.GenSeq

object Utils {
  object Format extends Enumeration {
    type Format = Value
    val Categorical, Continuous = Value
  }

  object Types {
    type Dataset[SAMPLE_TYPE <: Seq[Float]] = GenSeq[SAMPLE_TYPE]
    type Attribute = (Format.Format, Int)
  }
}
