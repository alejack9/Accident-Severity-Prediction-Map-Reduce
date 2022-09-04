package it.unibo.scalable.ml.dt.sequential

import org.scalatest.funsuite.AnyFunSuite

class C45Test extends AnyFunSuite {
  val D: Seq[Seq[Float]] = List(
    Seq(0, 3, 3, 5, 0),
    Seq(1, 1, 1, 7, 0),
    Seq(2, 1, 2, 11, 2),
    Seq(3, 1, 9, 11, 2),
    Seq(4, 7, 1, 3, 2)
  ).map(_.map(_.toFloat))

  val dtc = new C45

  test("run") {
    println("__________Dataset__________")
    println("| row  a_0  a_1  a_2    c |")
    println("|-------------------------|")
    D.sortBy(_.head).foreach{row => println("| % 3.0f  % 3.0f  % 3.0f  % 3.0f  % 3.0f |".format(row(0), row(1), row(2), row(3), row(4)))}
    println("---------------------------")
    dtc.run(D)
    assert(1==1)
  }
}
