/*
 * This file is part of COMP332 Assignment 3 2018.
 *
 * Lintilla, a simple functional programming language.
 *
 * © 2018, Dominic Verity, Macquarie University, All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Execution tests.
 */

package lintilla

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
  * Tests to check that the execution of SEC machine code translated from
  * Lintilla source code gives the right output.
  */
@RunWith(classOf[JUnitRunner])
class ExecTests extends SemanticTests {

  import SECTree._

  // Execution testing

  test("printing a boolean true gives the right output") {
    execTestInline("""
       |print(true)""".stripMargin, "true\n")
  }

  test("printing a boolean false gives the right output") {
    execTestInline("""
       |print(false)""".stripMargin, "false\n")
  }

  test("printing a constant integer gives the right output") {
    execTestInline("""
       |print(30)""".stripMargin, "30\n")
  }

  test("printing a integer variable gives the right output") {
    execTestInline("""
       |let x = 100;
       |print x""".stripMargin, "100\n")
  }

  test("printing a boolean variable gives the right output") {
    execTestInline("""
       |let x = true;
       |print x""".stripMargin, "true\n")
  }

  test("printing the result of a function gives the right output") {
    execTestInline("""
       |fn inc (a : int) -> int { a + 1 };
       |print inc(10)""".stripMargin, "11\n")
  }

  test("printing the addition of two integers gives the right output") {
    execTestInline("""
        |print(2 + 2)""".stripMargin, "4\n")
  }

  test("printing the subtraction of two integers gives the right output") {
    execTestInline("""
        |print(4 - 2)""".stripMargin, "2\n")
  }

  test("printing the multiplication of two integers gives the right output") {
    execTestInline("""
        |print(4 * 2)""".stripMargin, "8\n")
  }

  test("printing the division of two integers gives the right output") {
    execTestInline("""
        |print(4 / 2)""".stripMargin, "2\n")
  }

  test("printing the negation of an integer gives the right output") {
    execTestInline("""
        |print(- 2)""".stripMargin, "-2\n")
  }

  // Target tree testing

  test("an positive integer let declaration gives the right translation") {
    targetTestInline("""
       |let x = 100""".stripMargin, List(IInt(100), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("an negative integer let declaration gives the right translation") {
    targetTestInline("""
       |let x = -100""".stripMargin, List(IInt(-100), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("an true boolean let declaration gives the right translation") {
    targetTestInline("""
       |let x = true""".stripMargin, List(IBool(true), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("an false boolean let declaration gives the right translation") {
    targetTestInline("""
       |let x = false""".stripMargin, List(IBool(false), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("an addition operation gives the right translation") {
    targetTestInline("""
       |let x = 2 + 2""".stripMargin, List(IInt(2), IInt(2), IAdd(), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("a subtraction operation gives the right translation") {
    targetTestInline("""
       |let x = 2 - 2""".stripMargin, List(IInt(2), IInt(2), ISub(), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("a multiplication operation gives the right translation") {
    targetTestInline("""
       |let x = 2 * 2""".stripMargin, List(IInt(2), IInt(2), IMul(), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("a division operation gives the right translation") {
    targetTestInline("""
       |let x = 2 / 2""".stripMargin, List(IInt(2), IInt(2), IDiv(), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("a equality comparison gives the right translation") {
    targetTestInline("""
       |let x = (2 = 2)""".stripMargin, List(IInt(2), IInt(2), IEqual(), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("a less-than comparison gives the right translation") {
    targetTestInline("""
       |let x = (2 < 2)""".stripMargin, List(IInt(2), IInt(2), ILess(), IClosure(None, List("x"), List(IPopEnv())), ICall()))
  }

  test("an if expression gives the right translation") {
    targetTestInline("""
       |if(2 < 4){
       | print(true)
       |} else {
       | print(false)
       |}""".stripMargin, List(IInt(2), IInt(4), ILess(), IBranch(List(IBool(true), IPrint()), List(IBool(false), IPrint()))))
  }

  // Printing tests

  test("printing a variable gives the right translation") {
    targetTestInline("""
       |let x = 100;
       |print x""".stripMargin, List(IInt(100), IClosure(None, List("x"), List(IVar("x"), IPrint(), IPopEnv())), ICall()))
  }

  test("print constant integer gives the right translation") {
    targetTestInline("""
       |print(30)""".stripMargin,
                     List(IInt(30), IPrint()))
  }

  test("printing the addition of two integers gives the right translation") {
    targetTestInline("""
        |print(2 + 2)""".stripMargin,
                  List(IInt(2), IInt(2), IAdd(), IPrint()))
  }

  test("printing the subtraction of two integers gives the right translation") {
    targetTestInline("""
        |print(4 - 2)""".stripMargin,
                  List(IInt(4), IInt(2), ISub(), IPrint()))
  }

  test("printing the multiplication of two integers gives the right translation") {
    targetTestInline("""
        |print(4 * 2)""".stripMargin,
                  List(IInt(4), IInt(2), IMul(), IPrint()))
  }

  test("printing the division of two integers gives the right translation") {
    targetTestInline("""
        |print(4 / 2)""".stripMargin,
                  List(IInt(4), IInt(2), IDiv(), IPrint()))
  }

  test("printing the negation of an integer gives the right translation") {
    targetTestInline("""
        |print(- 2)""".stripMargin,
                  List(IInt(-2), IPrint()))
  }

  test("A function declaration and application gives the right translation") {
    targetTestInline("""
        |fn max(a : int, b : int) -> int {
        | if(a < b){
        |   b
        | } else {
        |   a
        | }
        |};
        |print(max(2,5))""".stripMargin, List(IClosure(Some("max"),List("a", "b"),
        List(IVar("a"), IVar("b"), ILess(), IBranch(List(IVar("b")),List(IVar("a"))),
        IPopEnv())), IClosure(None,List("max"),List(IInt(2), IInt(5), IVar("max"),
        ICall(), IPrint(), IPopEnv())), ICall()))
  }

}
