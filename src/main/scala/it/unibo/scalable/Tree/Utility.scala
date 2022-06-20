package it.unibo.scalable.Tree

import it.unibo.scalable.Format.Format

object Utility {

  def getBestSplitting[T](data: Array[(Array[T], T)], features: Array[(String, Format)] , metric:Array[(Array[T], T)] => Float) : (Array[T] => Boolean, T) ={
    throw new NotImplementedError()

  }

  def getDominantTarget[T](data: Array[(Array[T], T)]): T ={
    data.groupBy(_._2).map(elems => (elems._1, elems._2.length)).maxBy(_._2)._1
  }
  def isPure[T](data: Array[(Array[T], T)]): Boolean ={
    data.forall(_._2 == data.head._2)
  }

  def splitData[T](data: Array[(Array[T], T)], cond: Array[T] => Boolean): (Array[(Array[T], T)], Array[(Array[T], T)]) ={
    data.partition(sample => cond(sample._1))
  }

}
