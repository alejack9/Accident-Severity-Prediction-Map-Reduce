package it.unibo.scalable.ml.dt.base

import it.unibo.scalable.ml.dt.Utils.Format
import it.unibo.scalable.ml.dt.Utils.Types.Dataset
import it.unibo.scalable.ml.dt._
import org.scalatest.funsuite.AnyFunSuite

class C45ParTest extends AnyFunSuite {
  val D: Dataset[Seq[Float]] = Vector(
    Seq(3, 3, 5, 0),
    Seq(1, 1, 7, 0),
    Seq(1, 2, 11, 2),
    Seq(1, 9, 11, 2),
    Seq(7, 1, 3, 2)
  ).map(_.map(_.toFloat)).par

  val dtc = new base.C45

//  private def printDs(): Unit = {
//    println("________Dataset_______")
//    println("| a_0  a_1  a_2    c |")
//    println("|--------------------|")
//    D.sortBy(_.head).foreach{row => println("| % 3.0f  % 3.0f  % 3.0f  % 3.0f |".format(row(0), row(1), row(2), row(3)))}
//    println("----------------------")
//  }

  test("a_0 | continuous") {
    val t = dtc.train(D.map(sample => Vector(sample.head, sample.last)), Vector(Format.Continuous))
    assert(t == CondNode(ContinuousCondition(0, 5.0f), Vector(
      CondNode(ContinuousCondition(0, 2.0f), Vector(
        Leaf(2.0), Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("a_0 | categorical") {
    val t = dtc.train(D.map(sample => Vector(sample.head, sample.last)), Vector(Format.Categorical))
    assert(t == CondNode(CategoricalCondition(0, Vector(1.0, 3.0, 7.0)), Vector(
      Leaf(2.0), Leaf(0.0), Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("a0 a1 | continuous") {
    val t = dtc.train(D.map(sample => Vector(sample.head, sample(1), sample.last)), Vector(Format.Continuous, Format.Continuous))
    assert(t == CondNode(ContinuousCondition(0, 5.0), Vector(
      CondNode(ContinuousCondition(0, 2.0), Vector(
        CondNode(ContinuousCondition(1, 1.5), Vector(
          Leaf(0.0), Leaf(2.0))),
        Leaf(0.0))),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("a0 a1 | mixed") {
    val t = dtc.train(D.map(sample => Vector(sample.head, sample(1), sample.last)), Vector(Format.Continuous, Format.Categorical))
    assert (t == CondNode(CategoricalCondition(1, Vector(1.0, 2.0, 3.0, 9.0)), Vector(
      CondNode(ContinuousCondition(0, 4.0),
        Vector(Leaf(0.0),Leaf(2.0))),Leaf(2.0),
      Leaf(0.0),
      Leaf(2.0))
    ).asInstanceOf[Tree[Float]])
  }

  test("all ds | continuous") {
    val t = dtc.train(D, Vector(Format.Continuous, Format.Continuous, Format.Continuous))
    assert(t == CondNode(ContinuousCondition(2, 9.0), Vector(
      CondNode(ContinuousCondition(0, 5.0), Vector(
        Leaf(0.0), Leaf(2.0))),
      Leaf(2.0))
    ).asInstanceOf[Tree[Float]])
  }

  test("all ds | categorical") {
    val t = dtc.train(D, Vector(Format.Categorical, Format.Categorical, Format.Categorical))
    assert(t == CondNode(CategoricalCondition(2, Vector(3.0, 5.0, 7.0, 11.0)), Vector(
      Leaf(2.0),
      Leaf(0.0),
      Leaf(0.0),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("all ds | mixed") {
    val t = dtc.train(D, Vector(Format.Categorical, Format.Continuous, Format.Categorical))
    assert(t == CondNode(CategoricalCondition(2, Vector(3.0, 5.0, 7.0, 11.0)), Vector(
      Leaf(2.0),
      Leaf(0.0),
      Leaf(0.0),
      Leaf(2.0)
    )).asInstanceOf[Tree[Float]])
  }

  test("toString") {
    val t = dtc.train(D, Vector(Format.Categorical, Format.Continuous, Format.Categorical))
    assert(t.toString.equals("CondNode(cond:(feat 2 [ 3.0 , 5.0 , 7.0 , 11.0 ]),children:[Leaf(2.0), Leaf(0.0), Leaf(0.0), Leaf(2.0)])"))
  }

  test("toYaml") {
    val t = dtc.train(D, Vector(Format.Categorical, Format.Categorical, Format.Categorical))
    assertResult("index: 2\r\nchildren:\r\n- val: 3.0\r\n  leaf: 2.0\r\n- val: 5.0\r\n  leaf: 0.0\r\n- val: 7.0\r\n  leaf: 0.0\r\n- val: 11.0\r\n  leaf: 2.0\r\n")(t.toYaml)
  }
}
