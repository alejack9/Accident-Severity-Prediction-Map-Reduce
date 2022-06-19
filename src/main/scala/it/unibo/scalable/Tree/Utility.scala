package it.unibo.scalable.Tree

object Utility {
  def getDominantTarget(data: Array[(Array[Any], String)]): String ={
    throw new NotImplementedError()
  }
  def isPure(data: Array[(Array[Any], String)]): Boolean ={
    throw new NotImplementedError()
  }
  def getBestAttribute(data: Array[(Array[Any], String)]): (Any => Boolean, Float) ={
    throw new NotImplementedError()
  }

  def splitData(cond: Any => Boolean, data: Array[(Array[Any], String)]): (Array[(Array[Any], String)], Array[(Array[Any], String)]) ={
    throw new NotImplementedError()
  }

}
