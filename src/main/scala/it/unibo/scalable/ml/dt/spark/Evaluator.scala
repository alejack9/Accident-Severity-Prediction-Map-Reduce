package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.ml.dt.spark.Types.Dataset
import org.apache.spark.rdd.RDD

import scala.annotation.tailrec

object Evaluator {
  @tailrec
  private def _predict(nodeID: List[(Int, Float)], sample: Seq[Float], treeTable: Map[List[(Int, Float)], Node]): Float = {
    treeTable.get(nodeID) match {
      case Some(Leaf(target)) => target
      case Some(Link(featIndex)) => _predict(nodeID :+ (featIndex, sample(featIndex)), sample, treeTable)
      case None => -1 // key not found
    }
  }
  
  def predict(treeTable: Map[List[(Int, Float)], Node], ds: RDD[Seq[Float]]): RDD[Float] = {
    ds.map(sample => _predict(List.empty, sample, treeTable))
  }

  def score(ds: Dataset, ys: RDD[Float]): Float = {
    ds.zip(ys).filter { case (row, predicted) => row.last == predicted }.count().toFloat / ds.count()
  }

  def toYaml(treeTable: Map[List[(Int, Float)], Node]): String = {

    def _toYaml(treeTable: Map[List[(Int, Float)], Node], nodeID:List[(Int, Float)], level: Int): String = {
      treeTable.get(nodeID) match {
        case Some(Link(index)) => f"\r\n${" " * level * 2}index: $index \r\n${" " * level * 2}children: " +
          f"${treeTable.filterKeys(k =>
            k.length == level + 1
            || (k.length > level + 1 && k(level+1)._1 == index)
            && k(level)._1 == index).map {
            case (k, _) => f"\r\n${" " * level * 2}- val: ${k(level)._2} ${_toYaml(treeTable, k, level + 1)}"}.mkString(",")} "
        case Some(Leaf(target)) => f"\r\n${" " * level * 2}leaf: $target"
      }
    }

    _toYaml(treeTable, List.empty, 0)
  }
}
