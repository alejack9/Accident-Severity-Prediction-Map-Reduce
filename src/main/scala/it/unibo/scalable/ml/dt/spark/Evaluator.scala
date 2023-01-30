package it.unibo.scalable.ml.dt.spark

import it.unibo.scalable.ml.dt.spark.Types.Dataset
import org.apache.spark.rdd.RDD

import scala.annotation.tailrec
import scala.collection.GenSeq

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
    @tailrec
    def _toYml(nodes: GenSeq[(Int, List[(Int, Float)], String)], str: String): String = nodes match {
      case node :: xs => node match {
        case (level, k, valString) => treeTable.get(k) match {
          case Some(Leaf(v)) => _toYml(xs, str + f"$valString${"  " * level}leaf: ${v}\r\n")
          case Some(Link(index)) =>
            val newKeys = treeTable.filterKeys(k => k.length == level + 1 || (k.length > level + 1 && k(level + 1)._1 == index) && k(level)._1 == index).keys.toList
            _toYml(newKeys.map(key => (level + 1, key, f"${"  " * level}- val: ${key(level)._2}\r\n")) ++ xs, str + f"$valString${"  " * level}index: ${index}\r\n${"  " * level}children:\r\n")
        }
      }
      case Nil => str
    }

    _toYml(Seq((0, List.empty, "")), "")
  }
}
