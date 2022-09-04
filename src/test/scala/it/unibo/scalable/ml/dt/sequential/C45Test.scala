package it.unibo.scalable.ml.dt.sequential

import it.unibo.scalable.ml.dt.{CondNode, Condition, Leaf, Tree}
import org.scalatest.funsuite.AnyFunSuite

class C45Test extends AnyFunSuite {
  val D: Seq[Seq[Float]] = List(
    Seq(3, 3, 5, 0),
    Seq(1, 1, 7, 0),
    Seq(1, 2, 11, 2),
    Seq(1, 9, 11, 2),
    Seq(7, 1, 3, 2)
  ).map(_.map(_.toFloat))

  val dtc = new C45

  test("a_0, continuous") {
//    println("________Dataset_______")
//    println("| a_0  a_1  a_2    c |")
//    println("|--------------------|")
//    D.sortBy(_.head).foreach{row => println("| % 3.0f  % 3.0f  % 3.0f  % 3.0f |".format(row(0), row(1), row(2), row(3)))}
//    println("----------------------")
    val t = dtc.run(D.map(sample => List(sample.head, sample.last)), List(Format.Continuous))
//    t.show
    assert(t == CondNode(new Condition(_ => 0, "feat 0 < 5.0"), List(CondNode(new Condition(_ => 0, "feat 0 < 2.0"), List(Leaf(2.0), Leaf(0.0))), Leaf(2.0))).asInstanceOf[Tree[Float]])
  }

  test("a_0, categorical") {
    //    println("________Dataset_______")
    //    println("| a_0  a_1  a_2    c |")
    //    println("|--------------------|")
    //    D.sortBy(_.head).foreach{row => println("| % 3.0f  % 3.0f  % 3.0f  % 3.0f |".format(row(0), row(1), row(2), row(3)))}
    //    println("----------------------")
    val t = dtc.run(D.map(sample => List(sample.head, sample.last)), List(Format.Categorical))
    //    t.show
    assert(t == CondNode(new Condition(_ => 0, "feat 0 List(3.0, 1.0, 7.0)"), List(Leaf(0.0), Leaf(2.0), Leaf(2.0))).asInstanceOf[Tree[Float]])
  }

  test("a0 a1 continuous") {
    println("________Dataset_______")
    println("| a_0  a_1  a_2    c |")
    println("|--------------------|")
    D.sortBy(_.head).foreach{row => println("| % 3.0f  % 3.0f  % 3.0f  % 3.0f |".format(row(0), row(1), row(2), row(3)))}
    println("----------------------")
    val t = dtc.run(D.map(sample => List(sample.head, sample(1), sample.last)), List(Format.Continuous, Format.Continuous))
    //    t.show
//    assert(t == CondNode(new Condition(_ => 0, "feat 0 List(3.0, 1.0, 7.0)"), List(Leaf(0.0), Leaf(2.0), Leaf(2.0))).asInstanceOf[Tree[Float]])
  }

  test("a0 a1 mixed") {
    println("________Dataset_______")
    println("| a_0  a_1  a_2    c |")
    println("|--------------------|")
    D.sortBy(_.head).foreach{row => println("| % 3.0f  % 3.0f  % 3.0f  % 3.0f |".format(row(0), row(1), row(2), row(3)))}
    println("----------------------")
    val t = dtc.run(D.map(sample => List(sample.head, sample(1), sample.last)), List(Format.Continuous, Format.Categorical))
    //    t.show
//    assert(t == CondNode(new Condition(_ => 0, "feat 0 List(3.0, 1.0, 7.0)"), List(Leaf(0.0), Leaf(2.0), Leaf(2.0))).asInstanceOf[Tree[Float]])
  }
}
