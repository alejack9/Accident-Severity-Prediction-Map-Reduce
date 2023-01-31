package it.unibo.scalable.ml.dt.base

import it.unibo.scalable.ml.dt.Utils.Format
import it.unibo.scalable.ml.dt.Utils.Types.Dataset
import it.unibo.scalable.ml.dt._
import org.scalatest.funsuite.AnyFunSuite

class C45SeqTest extends AnyFunSuite {
  private def getDouble = {
    val D: Dataset[Seq[Double]] = Vector(
      Seq(3.0, 3.0, 5.0, 0.0),
      Seq(1.0, 1.0, 7.0, 0.0),
      Seq(1.toDouble / 3, 2.0, 11.0, 2.0),
      Seq(1.toDouble / 3, 9.0, 11, 2.0),
      Seq(7.0, 1.0, 3.0, 2.0)
    )

    val dtc = new base.C45[Double]

    (D, dtc)
  }

  private def getFloat = {
    val D: Dataset[Seq[Float]] = Vector(
      Seq(3, 3, 5, 0),
      Seq(1, 1, 7, 0),
      Seq(1, 2, 11, 2),
      Seq(1, 9, 11, 2),
      Seq(7, 1, 3, 2)
    )

    val dtc = new base.C45[Float]

    (D, dtc)
  }
//  private def printDs(): Unit = {
//        println("________Dataset_______")
//        println("| a_0  a_1  a_2    c |")
//        println("|--------------------|")
//        D.sortBy(_.head).foreach{row => println("| % 3.0f  % 3.0f  % 3.0f  % 3.0f |".format(row(0), row(1), row(2), row(3)))}
//        println("----------------------")
//  }

  test("a_0 | continuous | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d.map(sample => List(sample.head, sample.last)), List(Format.Continuous))
    assert(t == CondNode(ContinuousCondition(0, 5.0f), List(
      CondNode(ContinuousCondition(0, 2.0f), List(
        Leaf(2.0), Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("a_0 | categorical | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d.map(sample => List(sample.head, sample.last)), List(Format.Categorical))
    assert(t == CondNode(CategoricalCondition(0, List(1.0, 3.0, 7.0)), List(
      Leaf(2.0), Leaf(0.0), Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("a0 a1 | continuous | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d.map(sample => List(sample.head, sample(1), sample.last)), List(Format.Continuous, Format.Continuous))
    assert(t == CondNode(ContinuousCondition(0, 5.0), List(
      CondNode(ContinuousCondition(0, 2.0), List(
          CondNode(ContinuousCondition(1, 1.5), List(
            Leaf(0.0), Leaf(2.0))),
          Leaf(0.0))),
        Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("a0 a1 | mixed | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d.map(sample => List(sample.head, sample(1), sample.last)), List(Format.Continuous, Format.Categorical))
    assert (t == CondNode(CategoricalCondition(1, List(1.0, 2.0, 3.0, 9.0)), List(
      CondNode(ContinuousCondition(0, 4.0),
            List(Leaf(0.0),Leaf(2.0))),Leaf(2.0),
          Leaf(0.0),
        Leaf(2.0))
    ).asInstanceOf[Tree[Float]])
  }

  // -------------------------------all ds-------------------------------

  test("all ds | continuous | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d, List(Format.Continuous, Format.Continuous, Format.Continuous))
    assert(t == CondNode(ContinuousCondition(2, 9.0), List(
      CondNode(ContinuousCondition(0, 5.0), List(
        Leaf(0.0), Leaf(2.0))),
      Leaf(2.0))
    ).asInstanceOf[Tree[Float]])
  }

  test("all ds | continuous | Double") {
    val (d, dtc) = getDouble
    val t = dtc.train(d, List(Format.Continuous, Format.Continuous, Format.Continuous))
    assert(t == CondNode(ContinuousCondition(0, 0.6666666666666666), List(
      Leaf(2.0), CondNode(ContinuousCondition(0, 5.0), List(
        Leaf(0.0), Leaf(2.0))))
    ).asInstanceOf[Tree[Float]])
  }

  test("all ds | categorical | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d, List(Format.Categorical, Format.Categorical, Format.Categorical))
    assert(t == CondNode(CategoricalCondition(2, List(3.0, 5.0, 7.0, 11.0)), List(
      Leaf(2.0),
      Leaf(0.0),
      Leaf(0.0),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("all ds | categorical | Double") {
    val (d, dtc) = getDouble
    val t = dtc.train(d, List(Format.Categorical, Format.Categorical, Format.Categorical))
    assert(t == CondNode(CategoricalCondition(0, List(0.3333333333333333, 1.0, 3.0, 7.0)), List(
      Leaf(2.0),
      Leaf(0.0),
      Leaf(0.0),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("all ds | mixed | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d, List(Format.Categorical, Format.Continuous, Format.Categorical))
    assert(t == CondNode(CategoricalCondition(2, List(3.0, 5.0, 7.0, 11.0)), List(
      Leaf(2.0),
      Leaf(0.0),
      Leaf(0.0),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("all ds | mixed | Double") {
    val (d, dtc) = getDouble
    val t = dtc.train(d, List(Format.Categorical, Format.Continuous, Format.Categorical))
    assert(t == CondNode(CategoricalCondition(0, List(0.3333333333333333, 1.0, 3.0, 7.0)), List(
      Leaf(2.0),
      Leaf(0.0),
      Leaf(0.0),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("toString | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d, List(Format.Categorical, Format.Continuous, Format.Categorical))
    assert(t.toString.equals("CondNode(cond:(feat 2 [ 3.0 , 5.0 , 7.0 , 11.0 ]),children:[Leaf(2.0), Leaf(0.0), Leaf(0.0), Leaf(2.0)])"))
  }

  test("toString | Double") {
    val (d, dtc) = getDouble
    val t = dtc.train(d, List(Format.Categorical, Format.Continuous, Format.Categorical))
    println(t)
    assert(t.toString.equals("CondNode(cond:(feat 0 [ 0.3333333333333333 , 1.0 , 3.0 , 7.0 ]),children:[Leaf(2.0), Leaf(0.0), Leaf(0.0), Leaf(2.0)])"))
  }

  test("toYaml | Float") {
    val (d, dtc) = getFloat
    val t = dtc.train(d, Vector(Format.Categorical, Format.Categorical, Format.Categorical))
    assertResult("index: 2\r\nchildren:\r\n- val: 3.0\r\n  leaf: 2.0\r\n- val: 5.0\r\n  leaf: 0.0\r\n- val: 7.0\r\n  leaf: 0.0\r\n- val: 11.0\r\n  leaf: 2.0\r\n")(t.toYaml)
  }

  test("toYaml | Double") {
    val (d, dtc) = getDouble
    val t = dtc.train(d, Vector(Format.Categorical, Format.Categorical, Format.Categorical))
    assertResult("index: 0\r\nchildren:\r\n- val: 0.3333333333333333\r\n  leaf: 2.0\r\n- val: 1.0\r\n  leaf: 0.0\r\n- val: 3.0\r\n  leaf: 0.0\r\n- val: 7.0\r\n  leaf: 2.0\r\n")(t.toYaml)
  }
}
