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
    assertResult("index: 1\r\nchildren:\r\n- val: \"< 6.0\"\r\n  index: 1\r\n  children:\r\n  - val: \"< 2.5\"\r\n    index: 0\r\n    children:\r\n    - val: 1.0\r\n      index: 1\r\n      children:\r\n      - val: \"< 1.5\"\r\n        leaf: 0.0\r\n      - val: \">= 1.5\"\r\n        leaf: 2.0\r\n    - val: 7.0\r\n      leaf: 2.0\r\n  - val: \">= 2.5\"\r\n    leaf: 0.0\r\n- val: \">= 6.0\"\r\n  leaf: 2.0")(fileMockup.toString)
  }
}
