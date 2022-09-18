package it.unibo.scalable.ml.dt

sealed trait Condition[T] extends (Seq[T] => Int) {
  protected def fun: Seq[T] => Int
  protected val desc: String
  def !=(v1: Condition[T]): Boolean = v1.toString != this.toString
  override final def apply(v1: Seq[T]): Int = fun(v1)
  override final def toString: String = desc
}

case class ContinuousCondition[T : Ordering](private val index: Int, private val threshold: T)
  extends Condition[T] {
  private val ord: Ordering[T] = implicitly[Ordering[T]]
  override def fun: Seq[T] => Int = (sample: Seq[T]) => if(ord.lt(sample(index), threshold)) 0 else 1
  override val desc = s"feat $index < $threshold"
}

case class CategoricalCondition[T](private val index: Int, private val attrValues: Seq[T])
  extends Condition[T] {
  override def fun: Seq[T] => Int = (sample: Seq[T]) => attrValues.indexOf(sample(index))
  override val desc = s"feat $index $attrValues"
}
