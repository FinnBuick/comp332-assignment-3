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

  test("printing a variable gives the right translation") {
    targetTestInline("""
       |let x = 100;
       |print x""".stripMargin, List(IInt(100), IClosure(None, List("x"), List(IVar("x"), IPrint())), ICall()))
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
                  List(IInt(0), IInt(2), ISub(), IPrint()))
  }

}
