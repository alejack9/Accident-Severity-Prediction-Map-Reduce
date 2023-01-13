package it.unibo.scalable.ml.dt

import org.scalatest.funsuite.AnyFunSuite

class ConditionTest extends AnyFunSuite {
  test("ContinuousCondition | Float") {
    val cond: Condition[Float] = ContinuousCondition(0, 5)
    assert(cond.toString == "feat 0 < 5.0")
  }
  test("CategoricalCondition | Float") {
    val cond: Condition[Float] = CategoricalCondition(0,List(5,4,3))
    assert(cond.toString == "feat 0 [ 5.0 , 4.0 , 3.0 ]")
  }
  test("ContinuousCondition | Int") {
    val cond: Condition[Int] = ContinuousCondition(0, 5)
    assert(cond.toString == "feat 0 < 5")
  }
  test("CategoricalCondition | Int") {
    val cond: Condition[Int] = CategoricalCondition(0,List(5,4,3))
    assert(cond.toString == "feat 0 [ 5 , 4 , 3 ]")
  }
  test("ContinuousCondition | Double") {
    val cond: Condition[Double] = ContinuousCondition(0, 5)
    assert(cond.toString == "feat 0 < 5.0")
  }
  test("CategoricalCondition | Double") {
    val cond: Condition[Double] = CategoricalCondition(0,List(5,4,3))
    assert(cond.toString == "feat 0 [ 5.0 , 4.0 , 3.0 ]")
  }
}
