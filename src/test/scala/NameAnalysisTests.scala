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
 * Name analysis tests.
 */

package lintilla

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
  * Tests that check that the name analysis works correctly.  I.e., we run
  * the checking process over some input and make sure that the expected
  * errors were produced (and only them).
  */
@RunWith(classOf[JUnitRunner])
class NameAnalysisTests extends SemanticTests {

  // Basic binding tests

  test ("an undeclared top-level variable is unbound") {
    val messages =
      semanticTestInline ("""
             |x
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 2, 1, "'x' is not declared at this point")
  }

  test ("an undeclared variable is unbound in 'print' expression") {
    val messages =
      semanticTestInline ("""
             |print x
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 2, 7, "'x' is not declared at this point")
  }

  test("a top-level let binds its variable in subsequent expressions") {
    val messages =
      semanticTestInline ("""
             |let x = 1;
             |print x * 1
             |""".stripMargin)
    assert (messages.length === 0, "expecting no errors")
  }

  test ("at top level rebinding a variable raises an error") {
    val messages =
      semanticTestInline ("""
             |let x = 1;
             |let x = 2
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 3, 5, "'x' is declared more than once in current scope")
  }

  test("an undeclared variable in a block is unbound") {
    val messages =
      semanticTestInline ("""
             |{
             |    x
             |}
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 3, 5, "'x' is not declared at this point")
  }

  test ("in the same block rebinding a variable raises an error") {
    val messages =
      semanticTestInline ("""
             |{
             |    let x = 1;
             |    let x = 2
             |}
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 4, 9, "'x' is declared more than once in current scope")
  }

  test("a let binds its variable in subsequent expressions in a block...") {
    val messages =
      semanticTestInline ("""
             |{
             |    let x = 1;
             |    print x * x
             |}
             |""".stripMargin)
    assert (messages.length === 0, "expecting no errors")
  }

  test("...but a binding doesn't escape the block it is bound in") {
    val messages =
      semanticTestInline ("""
             |{
             |    let x = 1
             |};
             |x
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 5, 1, "'x' is not declared at this point")
  }

  test("unbound variables raise errors on the right of 'let' expressions") {
    val messages =
      semanticTestInline ("""
             |let y = x + 1
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 2, 9, "'x' is not declared at this point")
  }

  test("'let' expressions do not bind variables in their own right hand sides") {
    val messages =
      semanticTestInline ("""
             |let y = y + 1
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 2, 9, "'y' is not declared at this point")
  }

  test("variables may be rebound in inner blocks") {
    val messages =
      semanticTestInline ("""
             |let y = 1;
             |{
             |    let y = 2;
             |    print y
             |}
             |""".stripMargin)
    assert (messages.length === 0, "expecting no errors")
  }

  test("variables bound at top-level are visible in blocks") {
    val messages =
      semanticTestInline ("""
             |let y = 1;
             |print { y + { y } }
             |""".stripMargin)
    assert (messages.length === 0, "expecting no errors")
  }

  test("parameters cannot be rebound in function bodies") {
    val messages =
      semanticTestInline ("""
             |fn f(x : int) { let x = 1 }
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 2, 21, "'x' is declared more than once in current scope")
  }

  test("parameters cannot be shadowed by other parameters") {
    val messages =
      semanticTestInline ("""
             |fn f(x : int, x : bool) { }
             |""".stripMargin)
    assert (messages.length === 1, "expecting one error")
    assertMessage (messages, 0, 2, 15, "'x' is declared more than once in current scope")
  }
}
