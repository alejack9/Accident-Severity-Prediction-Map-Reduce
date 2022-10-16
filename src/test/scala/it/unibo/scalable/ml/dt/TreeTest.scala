package it.unibo.scalable.ml.dt

import org.scalatest.funsuite.AnyFunSuite

class TreeTest extends AnyFunSuite {
  val testD: Seq[Seq[Float]] = List(
    Seq(3, 3, 5, 0),
    Seq(1, 1, 7, 0),
    Seq(1, 2, 11, 2),
    Seq(1, 9, 11, 2),
    Seq(7, 1, 3, 2)
  ).map(_.map(_.toFloat))

  test("a_0 | continuous") {
    val t = CondNode(ContinuousCondition(0, 5.0f), List(
      CondNode(ContinuousCondition(0, 2.0f), List(
        Leaf(2.0), Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]]

    assert(t.predict(testD) == List[Float](0, 2, 2, 2, 2))
    assert(t.score(testD) == 0.8)
  }

  test("a_0 | categorical") {
    val t = CondNode(CategoricalCondition(0, List(3.0, 1.0, 7.0)), List(
      Leaf(0.0), Leaf(2.0), Leaf(2.0)
    )).asInstanceOf[Tree[Float]]

    assert(t.predict(testD) == List[Float](0, 2, 2, 2, 2))
    assert(t.score(testD) == 0.8)
  }

  test("a0 a1 | continuous") {
    val t = CondNode(ContinuousCondition(0, 5f), List(
      CondNode(ContinuousCondition(0, 2f), List(
        CondNode(ContinuousCondition(1, 5.5f), List(
          CondNode(ContinuousCondition(1, 1.5f), List(
            Leaf(0.0), Leaf(2.0))),
          Leaf(2.0))),
        Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]]

    assert(t.predict(testD) == List[Float](0, 0, 2, 2, 2))
    assert(t.score(testD) == 1f)
  }

  test("a0 a1 | mixed") {
    val t = CondNode(ContinuousCondition(0, 5f), List(
      CondNode(ContinuousCondition(0, 2f), List(
        CondNode(CategoricalCondition(1, List(1.0, 2.0, 9.0)), List(
          Leaf(0.0),Leaf(2.0),Leaf(2.0))),
        Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]]

    assert(t.predict(testD) == List[Float](0, 0, 2, 2, 2))
    assert(t.score(testD) == 1f)
  }

  test("all ds | continuous") {
    val t = CondNode(ContinuousCondition(0, 5f), List(
      CondNode(ContinuousCondition(0, 2f), List(
        CondNode(ContinuousCondition(1, 5.5f), List(
          CondNode(ContinuousCondition(1, 1.5f), List(
            Leaf(0.0), Leaf(2.0))),
          Leaf(2.0))),
        Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]]

    assert(t.predict(testD) == List[Float](0, 0, 2, 2, 2))
    assert(t.score(testD) == 1f)
  }

  test("all ds | categorical") {
    val t = CondNode(CategoricalCondition(0, List(3.0, 1.0, 7.0)), List(
      Leaf(0.0),
      CondNode(CategoricalCondition(2, List(7.0, 11.0)), List(
        Leaf(0.0), Leaf(2.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]]

    assert(t.predict(testD) == List[Float](0, 0, 2, 2, 2))
    assert(t.score(testD) == 1f)
  }

  test("all ds | mixed") {
    val t = CondNode(ContinuousCondition(1, 6f), List(
      CondNode(ContinuousCondition(1, 2.5f), List(
        CondNode(CategoricalCondition(0, List(1.0, 7.0)), List(
          CondNode(ContinuousCondition(1, 1.5f), List(
            Leaf(0.0), Leaf(2.0))),
          Leaf(2.0))),
        Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]]

    assert(t.predict(testD) == List[Float](0, 0, 2, 2, 2))
    assert(t.score(testD) == 1f)
  }
}
