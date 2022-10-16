package it.unibo.scalable.ml.dt

import org.scalatest.funsuite.AnyFunSuite

import java.io.{ByteArrayOutputStream, PrintWriter}

class TreeSaverTest extends AnyFunSuite {

  test("tree saving") {
    val t = CondNode(ContinuousCondition(1, 6f), List(
      CondNode(ContinuousCondition(1, 2.5f), List(
        CondNode(CategoricalCondition(0, List(1.0, 7.0)), List(
          CondNode(ContinuousCondition(1, 1.5f), List(
            Leaf(0.0), Leaf(2.0))),
          Leaf(2.0))),
        Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]]

    val fileMockup = new ByteArrayOutputStream()
    val pw = new PrintWriter(fileMockup)

    TreeSaver.save(t, pw)
    assert(fileMockup.toString.equals("0 - feat 1 < 6.0 | 1 - feat 1 < 2.5 | 2 - feat 0 List(1.0, 7.0) | 3 - feat 1 < 1.5 | 4 - 0.0 | 4 - 2.0 | 3 - 2.0 | 2 - 0.0 | 1 - 2.0 | "))
  }
}
