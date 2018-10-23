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
 * SEC code representation.
 */

package lintilla

/**
  * Module containing tree structures for representing SEC programs.
  */
object SECTree {

  /**
    * Representation of machine-level names.
    */
  type Name = String

  /**
    * SEC machine instructions.
    */
  sealed abstract class Instr

  /**
    * Addition instruction. Add the integer found on the top of the operand
    * stack to the one found next to top. Pop the operands and push the
    * result onto the operand stack.
    */
  case class IAdd() extends Instr

  /**
    * Push Boolean instruction. Push a Boolean value onto the operand stack.
    */
  case class IBool(value : Boolean) extends Instr

  /**
    * Branch instruction. Pop a Boolean value from the top of the operand
    * stack. If it's true, continue by executing the left frame, otherwise
    * continue by executing the right frame.
    */
  case class IBranch(left : Frame, right : Frame) extends Instr

  /**
    * Call instruction. Pop the top value from the operand stack and pop
    * a closure from the next to top position. Continue execution in the
    * body of the closure with the closure's argument bound to the value.
    */
  case class ICall() extends Instr

  /**
    * Push closure instruction. Push a closure that captures the current
    * environment and will execute body when presented with values for its the
    * named arguments. If the optional function name is defined, it will be
    * bound to the closure in the captured environment so that recursive calls
    * are possible.
    */
  case class IClosure(optFunName : Option[Name], argNames : List[Name], body : Frame)
      extends Instr

  /**
    * Division instruction. Divide the integer found on the top of the operand
    * stack by the one found next to top. Pop the operands and push the
    * result onto the operand stack.
    */
  case class IDiv() extends Instr

  /**
    * Equality instruction. Compare the integer or Boolean found on the top of
    * the operand stack to the value of the same type found next to top. Pop the
    * operands and push the result of the comparison onto the operand stack as
    * a Boolean value.
    */
  case class IEqual() extends Instr

  /**
    * Push integer instruction. Push aan integer value onto the operand stack.
    */
  case class IInt(value : Int) extends Instr

  /**
    * Less than instruction. Compare the integer found on the top of the operand
    * stack to the one found next to top. Pop the operands and push the result
    * of the comparison onto the operand stack as a Boolean value.
    */
  case class ILess() extends Instr

  /**
    * Multiplication instruction. Multiply the integer found on the top of the operand
    * stack to the one found next to top. Pop the operands and push the
    * result onto the operand stack.
    */
  case class IMul() extends Instr

  /**
    * Pop environment instruction. Pop the outermost environment from the environment.
    */
  case class IPopEnv() extends Instr

  /**
    * Print instruction. Pop the top value from the operand stack and print it,
    * followed by a newline.
    */
  case class IPrint() extends Instr

  /**
    * Subtraction instruction. Subtract the integer found on the top of the operand
    * stack to the one found next to top. Pop the operands and push the
    * result onto the operand stack.
    */
  case class ISub() extends Instr

  /**
    * Push variable instruction. Lookup the given variable in the current environment
    * and push its value onto the operand stack. Raise a fatal error if the name is
    * not currently bound.
    */
  case class IVar(name : Name) extends Instr

  /**
    * A frame is a list of instructions.
    */
  type Frame = List[Instr]

  /**
    * An operand stack is a list of machine values.
    */
  type Stack = List[MValue]

  /**
    * An environment is a map between names and machine values.
    */
  type Environ = Map[Name,MValue]

  /**
    * SEC machine values.
    */
  sealed abstract class MValue

  /**
    * The unique value of type 'unit'
    */
  case object MNil extends MValue {
    override def toString() = "Nothing!"
  }

  /**
    * An integer machine value.
    */
  case class MInt(value : Int) extends MValue {
    override def toString() = value.toString()
  }

  /**
    * A Boolean machine value.
    */
  case class MBool(value : Boolean) extends MValue {
    override def toString() = value.toString()
  }

  /**
    * A closure machine value.
    *
    * - `argNames` is a list of names of the arguments to the function.
    * - `frame` is the list of instructions that comprise the body of the
    * function.
    * - `env` is the environment in which the function was defined (and
    * hence also the environment in which the body should be evaluated).
    *
    * The `env` field is mutable so that a circular structure can be created
    * for recursive closures (since the environment needs to bind the function
    * name to the closure).
    */
  case class MClosure(argNames : List[Name], frame : Frame, var env : Environ)
      extends MValue {
    override def toString() =
      s"""function of arguments (${argNames.mkString(",")})"""
  }

}
