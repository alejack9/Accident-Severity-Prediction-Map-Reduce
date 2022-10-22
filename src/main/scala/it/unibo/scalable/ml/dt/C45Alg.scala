package it.unibo.scalable.ml.dt

import it.unibo.scalable.ml.dt.Utils.Format.Format
import it.unibo.scalable.ml.dt.Utils.Types.Dataset

trait C45Alg {
  def train[T <: Seq[Float]](ds: Dataset[T], attributeTypes: Seq[Format]): Tree[Float]
}