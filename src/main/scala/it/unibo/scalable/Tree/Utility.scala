package it.unibo.scalable.Tree

object Utility {
  def getDominantTarget[T](data: Array[(Array[T], T)]): T ={
    throw new NotImplementedError()
  }
  def isPure[T](data: Array[(Array[T], T)]): Boolean ={
    throw new NotImplementedError()
  }
  def getBestSplitting[T](data: Array[(Array[T], T)], metric:Array[(Array[T], T)] => Float) : (Array[T] => Boolean, T) ={
    throw new NotImplementedError()
  }

  def splitData[T](data: Array[(Array[T], T)], cond: T => Boolean): (Array[(Array[T], T)], Array[(Array[T], T)]) ={
    throw new NotImplementedError()
  }

}
