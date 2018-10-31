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

  test("printing a constant integer gives the right output") {
    execTestInline("""
       |print(30)""".stripMargin, "30\n")
  }

  test("print constant integer gives the right translation") {
    targetTestInline("""
       |print(30)""".stripMargin,
                     List(IInt(30), IPrint()))
  }

}
