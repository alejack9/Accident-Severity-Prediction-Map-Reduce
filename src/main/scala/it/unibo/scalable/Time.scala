package it.unibo.scalable

object Time {
  def compute[T](f: => T): (T, Double) = {
    val t1 = System.nanoTime
    (f, (System.nanoTime - t1) / 1e9d)
  }
}
