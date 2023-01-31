package it.unibo.scalable.ml.dt

import scala.collection.GenSeq
import scala.language.implicitConversions

object Utils {
  object Format extends Enumeration {
    type Format = Value
    val Categorical, Continuous = Value
  }

  object Types {
    type Dataset[+SAMPLE_TYPE <: Seq[AnyVal]] = GenSeq[SAMPLE_TYPE]
    type Attribute = (Format.Format, Int)
  }

  implicit class GenSeqSort[+T : Ordering](s: GenSeq[T]) {
    def sort(): GenSeq[T] = s match {
      case c: Seq[T] => c.sorted
      case _ => s.seq.sorted.par
    }
  }
}
