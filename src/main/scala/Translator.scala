/*
 * This file is part of COMP332 Assignment 3 2018.
 *
 * Lintilla, a simple functional programming language.
 *
 * © 2018, Dominic Verity and Anthony Sloane, Macquarie University.
 *         All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Lintilla to SEC machine code translator.
 */

package lintilla

/**
  * Translator from Lintilla source programs to SEC target programs.
  */
object Translator {

  import SECTree._
  import LintillaTree._
  import scala.collection.mutable.ListBuffer

  // Sequences in the source three are store as `Vector`s but we often want
  // them to be converted to `List`s. This is done automatically by the following
  // implicit conversion function.
  import scala.language.implicitConversions
  implicit private def vecToList[T](vec : Vector[T]) = vec.toList

  /**
    * Return a frame that represents the SEC instructions for a Lintilla
    * program.
    */
  def translate (program : Program) : Frame =
    program match {
      case Program(exps) => translateToFrame(exps)
    }

  /**
    * Translate a sequence of Lintilla expressions and return a frame containing
    * the generated SEC code.
    */
  def translateToFrame(exps : List[Expression]) : Frame = {

    // An instruction buffer for accumulating the translated SEC code.
    val instrBuffer = new ListBuffer[Instr] ()

    // Generate an instruction by appending it to the instruction buffer.
    def gen (instr : Instr) {
      instrBuffer.append (instr)
    }

    /**
      * Translate a sequence of expressions in order, adding the SEC code
      * generated by each one to the end of the instruction buffer.
      */
    def translateSeq(list : List[Expression]) {
      list match {

        // FIXME Add cases to translate binding constructs `let` and `fn` here

        case (LetDecl(name, exp) :: rest) =>
          translateExp(exp)
          val frame = translateToFrame(rest)
          val idn : String = name match {
            case IdnDef(i) => i
          }

          gen (IClosure (None, List(idn), frame))

        case (FnDecl(name, args, optRet, body), :: rest) => // Must make 2 IClosures


        case (exp :: rest) =>
          translateExp(exp)
          translateSeq(rest)

        case _ => ()
      }
    }

    /**
      * Translate a single Lintilla expression, adding the generated SEC code to
      * the end of the current instruction buffer.
      */
    def translateExp(exp : Expression) {

      // FIXME Add code to translate an single expression here.
      exp match {

        case PlusExp (left, right) =>
          translateExp (left)
          translateExp (right)
          gen (IAdd ())

        case MinusExp (left, right) =>
          translateExp (left)
          translateExp (right)
          gen (ISub ())

        case StarExp (left, right) =>
          translateExp (left)
          translateExp (right)
          gen (IMul ())

        case SlashExp (left, right) =>
          translateExp (left)
          translateExp (right)
          gen (IDiv ())

        case NegExp (exp) =>
          gen (IInt (0))
          exp match {
            case IntExp (value) => gen (IInt (value))
            case _ => ()
          }
          gen (ISub ())

        case BoolExp (value) =>
          gen (IBool (value))

        case IdnExp (IdnUse (i)) =>
          gen (IVar (i))

        case IntExp (i) =>
          gen (IInt (i))

        // case IfExp (cond, left, right) =>
        //   translateExp(left)
        //   translateExp(right)
        //

        // case PrintExp(exp) =>
        //   gen (IPrint (exp))

        case _ => ()

      }

    }

    // Call sequence translator
    translateSeq(exps)

    // Return generated code frame.
    instrBuffer.toList
  }

}
