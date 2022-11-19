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
    ds.zipWithIndex().map{case (sample, index) => (index, _predict(List.empty, sample, treeTable))}.sortBy(_._1).map(_._2)
  }

  def score(ds: Dataset, ys: RDD[Float]): Float = {
    ds.zip(ys).filter { case (row, predicted) => row.last == predicted }.count().toFloat / ds.count()
  }

}
