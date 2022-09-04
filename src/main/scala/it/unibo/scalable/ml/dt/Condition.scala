package it.unibo.scalable.ml.dt

sealed class Condition(private val fun: Seq[Float] => Int, desc: String = "") extends (Seq[Float] => Int) {
  def !=(v1: Condition): Boolean = v1.toString != this.toString
  override def apply(v1: Seq[Float]): Int = fun(v1)
  override def toString: String = desc
}
