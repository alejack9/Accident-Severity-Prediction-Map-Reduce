package it.unibo.scalable.ml.dt

import org.scalatest.funsuite.AnyFunSuite

class ConditionTest extends AnyFunSuite {
  test("apply") {
    val cond: Condition = new Condition(_ => 0, "<")
    assert(cond.toString == "<")
  }
}
