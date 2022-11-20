package it.unibo.scalable.ml.dt

import scala.collection.GenSeq

sealed trait Condition[T] extends (Seq[T] => Int) {
  val index: Int
  protected def fun: Seq[T] => Int
  protected val desc: String
  def !=(v1: Condition[T]): Boolean = v1.toString != this.toString
  override final def apply(v1: Seq[T]): Int = fun(v1)
  override final def toString: String = desc
  def getValues: Seq[String]
}

case class ContinuousCondition[T : Ordering](index: Int, private val threshold: T)
  extends Condition[T] {
  private val ord: Ordering[T] = implicitly[Ordering[T]]
  override def fun: Seq[T] => Int = (sample: Seq[T]) => if(ord.lt(sample(index), threshold)) 0 else 1
  override val desc = s"feat $index < $threshold"
  override def getValues: Seq[String] = Seq(f""""< $threshold"""", f"""">= $threshold"""")
}

case class CategoricalCondition[T](index: Int, private val attrValues: GenSeq[T])
  extends Condition[T] {
  override def fun: Seq[T] => Int = (sample: Seq[T]) => attrValues.indexOf(sample(index))
  override val desc = s"feat $index ${attrValues.mkString("[ "," , "," ]")}"
  override def getValues: Seq[String] = attrValues.map(_.toString).toList
}
