# Macquarie University, Department of Computing #

## COMP332 Programming Languages 2018 ##

## Assignment 3: Lintilla translation and execution

Due: 5pm Sunday 11th November (week 13)  
Worth: 15% of unit assessment

Marks breakdown:

* Code: 50% (of which tests are worth 10%)
* Report: 50% (of which test description is worth 10%)

Submit a notice of disruption via [Ask@MQ](https://ask.mq.edu.au) if you are unable to submit on time for medical or other legitimate reasons.

Late penalty without proper justification: 20% of the full marks for the assessment per day or part thereof late.

### Overview

This assignment asks you to develop a translator for the Lintilla programming language. The translator will target a simple abstract machine called the *SEC machine*, which is a simplified version of Landin's influential [SECD machine](https://en.wikipedia.org/wiki/SECD_machine). An implementation of this machine is provided, so once your translator is working you will be able to run Lintilla programs.

You will build on the parsing and tree construction done in Assignment
One and on a semantic analysis module provided in the framework bundle. Before you start this assignment we strongly recommend that you should:

*   Carefully read the latest version of the `README.md` file in the framework repository. This has been extended to describe the semantic (name and type analysis) rules of the Lintilla language.
*   Note that the version of Lintilla used for this assignment is slightly different to that in assignment 2. In particular, we have removed mutable variable bindings, assignments, `return` expressions, and `while` loops. This has been done to greatly simplifies your task which will be to write the translation phase of the compiler.
*   Also note that we have added a `print` expression, which allows you to print the computed value of arbitrary expressions to the terminal. You can use this to do `println`-style bug hunting when executing compiled Lintilla code.
*   Thoroughly peruse the `SemanticAnalysis.scala` module in the _src/main/scala_ directory of the framework bundle. Compare it to the semantic analyser you wrote for the expression language in the workshops, and make sure you have understood how it works and what it does.

Your translator is only expected to translate programs that have passed through the earlier phases of the compiler without producing any errors. In particular, you can assume that any source tree your translator will be presented with satisfies all of the semantic rules given in the `README.md` file.

Building this translator will give you insight into the way that source language features correspond to target language features. It will show you how abstract machines can be used to provide a run-time system for languages. You will also gain specific experience with how Lintilla is compiled and executed, which is similar in many respects to the implementation of full general-purpose functional programming languages. Once you have completed this assignment you will have a working implementation of Lintilla.

### The SEC machine

The "SEC" in the name SEC machine stands for Stack, Environment and Code. The
interpreter that implements our SEC machine can be found in the file
`SECMachine.scala` (_src/main/scala_ directory of the framework bundle). This machine simplifies Landin's SECD machine by omitting its Dump, a powerful control stack construct used to implement more complex control flow features like `return`, exception handling, and coroutines.

The stack, environment and code components form the state of the SEC machine. The name is somewhat confusing because the environment component of the state is a stack as well. We will refer to the *operand stack* and the *environment stack* to make it clear which one we are referring to.

The operand stack of the machine is used to keep track of values that are being operated on by instructions. In this respect, the SEC machine is similar to the stack machine which we used in the mixed classes in Weeks 10 and 11. When execution begins the operand stack is empty.

The operand stack can contain *machine values*. There are three types of value:
_integers_, _Booleans_ and _closures_. The last of these is used to represent
functions and will be explained more below.

The environment stack of the machine is used to keep track of bindings of names to values. For example, when a function is called the value passed to the function will be bound to the names of the function arguments. When the body of the function refers to the name we will look it up in the current environment to get its value. The environment stack is initialised to contain a single environment that is empty and at any time the topmost environment on the stack defines the bindings that are accessible to the running code. In our implementation an environment is a map from names to machine values.

The code component of the machine state is the sequence of SEC machine instructions that is being executed. When a program begins executing, the code component is initialised to the code sequence that comprises the compiled program. At each step of the execution the machine interpreter will look at the first instruction in the code sequence and decide what to do based on what that instruction is. In each case the instruction will update the machine state in some way. Execution will then continue with the next instruction in the sequence.

#### SEC machine instructions

The SEC machine instructions are defined in the file `SECTree.scala`. There are the following instruction types:

-   Push value instructions (`IBool` and `IInt`): Push a single given Boolean or integer value onto the operand stack.

-   Push variable instruction (`IVar`): Push the value of a given variable onto the operand stack. This variable must be bound in the current environment, otherwise the machine halts with an *unknown variable* error.

-   Arithmetic instructions: (`IAdd`, `IDiv`, `IMul` and `ISub`): Pop two integers off the operand stack, perform the given operation on them and push the result onto the operand stack. The first value popped will be the right operand of the operation and the second value popped will be the left operand. The machine will halt if the top of the operand stack does not contain two integer values. If the instruction is `IDiv` the machine will halt with an error if the right operand is zero.

-   Equality instruction: (`IEqual`): Pop two values off the operand stack, compare them for equality and push the result of the comparison as a Boolean onto the operand stack. The machine will halt if the top of the operand stack does not contain two values that are either both integers or both Booleans.

-   Less than instruction: (`ILess`): Pop two values off the operand stack, compare them for equality and push the result of the comparison as a Boolean onto the operand stack. The first value popped will be the right operand of the operation and the second value popped will be the left operand. The machine will halt if the top of the operand stack does not contain two integer values.

-   Print instruction: (`IPrint`): Pop a value off the operand stack and print it. The machine will halt if there is no value on the operand stack.

-   Branch instruction: (`IBranch`): The instruction contains two code sequences (also called *frames* in the code). Pop a Boolean value from the operand stack. If the popped value is true continue execution with the first code sequence from the instruction; otherwise continue with the second code sequence. The machine will halt if the top of the operand stack does not contain a Boolean value.

-   Closure instruction: (`IClosure`): The instruction contains a (possibly empty) list of argument names, and a sequence of instructions that constitute the body of a function. When this instruction is executed it pushes a closure value onto the operand stack that represents this function. The closure will contain the list of parameter names, the body code sequence, and the current environment (top of the environment stack). The environment value is therefore *captured* so that it can be used later to look up the values of free variables in the function body when the function is called. The machine will halt if the environment stack is empty, since in that case there will be no environment to capture.

-   Call instruction: (`ICall`): Pop a value from the operand stack; this value must be an closure value, since it will provide the function that is called. Then pop one value from the operand stack for each parameter of that closure, and continue execution with the closure's parameters bound to these values. Arguments to this call should be pushed into the stack in the order that they occur in the function call. So the first argument is the first to be pushed into the stack. The machine will halt if the first value popped isn't a closure _or_ if the operand stack doesn't contain as many values as that function call requires as arguments.

-   Pop environment instruction: (`IPopEnv`): Pop the current environment from the environment stack and discard it. The machine will halt if the environment stack is empty.

Note that in normal operation of the SEC machine we would not expect to trigger any of the error conditions. The code that we generate for the machine must be constructed so that none of the error conditions can occur. For example, if we generate an `IAdd` instruction then we must previously generate code to push its two integer operands onto the operand stack. That this is always the case is a _mathematically provable_ consequence of the semantic rules that must apply to a legal Lintilla program.

### Translating Lintilla to the SEC machine

This section describes how the constructs from the Lintilla language should be translated to SEC machine instructions. The more routine parts of this translation are as follows:

1.  Boolean expressions, integer expressions and applied occurrences of identifiers should translate into pushes of the relevant values.

2.  Arithmetic operations, equality and less-then comparisons should translate into SEC code to push each of their operands onto the operand stack, followed by the relevant machine operation. Left hand operands should be evaluated before right hand ones, so when the code generated from the expression

        { print 10; 10 } + { print 5; 5 }

    is executed it should print the number `10` before it prints the number `5`.

3.  An `if` expression should translate into SEC code to evaluate its condition followed by an `IBranch` instruction containing the SEC machine code generated from its _then_ and _else_ clauses.

4.  A `print` expression should translate into SEC code to evaluate its operand expression followed by an `IPrint` instruction.

5.  An application expression should translate into SEC code to evaluate each one of its arguments, in order, followed by SEC code that evaluates its function component, and followed by an `ICall` instruction. In other words, a function call `f(e_1,...,e_n)` should translate to code of the following form:

        <SEC code translation of e_1>
        <SEC code translation of e_2>
        ...
        <SEC code translation of e_n>
        <SEC code translation of f>
        ICall()
        
6.  A function declaration 

        fn f(v_1 : type_1,..., v_n : type_n) -> type {
            body
        }
        
    should translate to the instruction:
    
        IClosure(Some("f"),
                 List("v_1",...,"v_n"),
                 <SEC code translation of body> :+ IPopEnv())
    
    followed by code to bind the identifier `f` (in the remainder of the current block) to the closure object that is pushed onto the stack by this instruction. We shall discuss the construction of that binding code a little later.
    
    Notice here that we have added an `IPopEnv` instruction to the end of the translated body of our function. We've done this because the execution of a closure pushes the referencing environment it contains onto the environment stack and this has to be popped back off the stack when the closure is exited.
    
7.  A `let` declaration should translate to code that evaluates its initialisation expression and then binds the resulting value on the stack to the specified identifier. We examine this binding translation next.
        
The tricky part of this translation involves the translation of sequences of expressions (at the top level or within a block) which contain `let` and `fn` declarations. Suppose that we are given a sequence of expressions that comprise some section of the body of a block (or of the whole program) from some expression onwards to the end of the block:

    expression_1;
    expression_2;
    ...
    expression_n

Then, generally speaking, we would simply translate this sequence by translating each individual expression in turn and concatenating the results:

    <SEC code translation of expression_1>
    <SEC code translation of expression_2>
    ...
    <SEC code translation of expression_1>
    
Were we to represent this sequence of expressions as a list `exp :: rest`, comprising the first expression `exp` and the list of remaining expressions `rest`, then we might express this translation as a recursive template:

    <SEC code translation of the sequence exp :: rest> :=
        <SEC code translation of the expression exp>
        <SEC code translation of the sequence rest>

Another way of looking at this is that we could write a recursive translation function to do this task. This might look something like

    def translateSeq(list : List[Expression]) {
      list match {
          case (exp :: rest) =>
            translateExp(exp)
            translateSeq(rest)
          case _ => ()
      }
    }

where `translateExp` is a function to translate a single expression and add the generated code to an instruction buffer (as we did in the translation code for the expression language).

We must, however, adjust this translation in the case where the first expression `exp` in a list of expressions is a `let` or a `fn`. The partial translations of `let` and `fun` given above leave a value on the top of the stack, and that value should be popped and bound to a variable whose scope extends over the translations of the remaining expressions in `rest`. A quick perusal of the SEC machine code, however, reveals that the only way we have to bind a variable to a value on the stack is to call a closure that has that variable as its parameter.

This leads us to conclude that if the first expression `exp` is a `let` or `fn` declaration then we should modify our translation of `exp :: rest` as follows:

    <SEC code translation of the sequence exp :: rest> :=
        <(partial) SEC code translation of exp discussed above>
        IClosure(None, List(<var name>),   // <var_name> = name of the variable to bind.
                 <SEC code translation of the sequence rest> :+ IPopEnv())
        ICall()

In other words, this code will push the value to be bound onto the stack, then it will push a closure containing the SEC code generated from the subsequent expressions, and finally it executes a call to execute that closure. This translation can be implemented by adding two specialised cases (for `LetDecl` and `FnDecl`) nodes to the `match` expression in the function `translateSeq` above.

#### Translation Example

As a simple example of translation, consider this Lintilla program:

    print 3 * (12 / 4)

This program would be translated into the followed SEC machine code:

    List(IInt(3), IInt(12), IInt(4), IDiv(), IMul(), IPrint())

As a more complex translation example, consider the following Lintilla program

    let x = 100;
    print x;
    fn inc (a : int) -> int { a + 1 };
    print inc(x)

which should print numbers `100` and `101` when executed.

Here is the SEC machine code that is the translation of this program. The comments in the code are there to assist your understanding; they are not part of the code itself.

    List(
        IInt(100),                 // Translation of the initialisation expression on line 1
        IClosure(                  // Create closure to bind the variable 'x'.
            None,
            List("x"),
            List(                  // Frame containing translation of lines 2-4.
                IVar("x"),
                IPrint(),
                IClosure(          // Create closure implementing the function 'inc' 
                    Some("inc"),
                    List("a"),
                    List(
                        IVar("a"), // List of instructions translated from body of 'inc'
                        IInt(1), 
                        IAdd(), 
                        IPopEnv())), 
                IClosure(          // Create closure to bind the variable 'inc'
                    None,
                    List("inc"),
                    List(          // Frame containing translation of line 4
                        IVar("x"), 
                        IVar("inc"), 
                        ICall(), 
                        IPrint(), 
                        IPopEnv()))
                ICall(),           // Call to bind 'inc'
                IPopEnv())),
        ICall())                   // Call to bind 'x'
    
The _src/test/resources_ contains the sources of a handful of Lintilla programs along with the output generated when we compiled and executed them using our reference compiler. These are provided as resources for further investigating the relationship between pieces of Lintilla code and the SEC code they translate to.

### What you have to do

You have to write, document and test a Scala translator for the Lintilla language, according to the description above. You are strongly advised to complete portions of the assignment before moving onto the next one, rather than trying to code the whole solution in one go.

A skeleton sbt project for the assignment has been provided on BitBucket as the [dominicverity/comp332-lintilla](https://bitbucket.org/dominicverity/comp332-lintilla) repository. The modules are very similar to those used in the practical exercises. The skeleton contains the modules you will need, particularly for Weeks 10 and 11. For this assignment you should not have to modify any parts of the implementation except the translator (`Translator.scala`) and the related tests (`ExecTests.scala`).

The basic structure of the translation function is given to get you started; you must provide the rest, particularly the implementations of the actual translation of Lintilla constructs into SEC machine instructions (look for `FIXME` in the code for some places where new code has to go).
    
### What you must hand in and how

A zip file containing all of the code for your project and a type-written report.

Submit every source and build file that is needed to build your program from source, including files in the skeleton that you have not changed. Do not add any new files or include multiple versions of your files. Do not include any libraries or generated files (run the sbt `clean` command before you zip your project). We will compile all of the files that you submit using sbt, so you should avoid any other build mechanisms.

Your submission should include all of the tests that you have used to make sure that your program is working correctly. Note that just testing one or two simple cases is not enough for many marks. You should test as comprehensively as you can.

Your report should describe how you have achieved the goals of the assignment. Do not neglect the report since it is worth 50% of the marks for the assignment.

Your report should contain the following sections:

* A title page or heading that gives the assignment details, your name and student number.
* A brief introduction that summarises the aim of the assignment and the structure of the rest of the report.
* A description of the design and implementation work that you have done to achieve the goals of the assignment. Listing some code fragments may be useful to illustrate your description, but don't give a long listing. Leaving out obvious stuff is OK, as long as what you have done is clear. A good rule of thumb is to include enough detail to allow a fellow student to understand it if they are at the stage you were at when you started work on the assignment.
* A description of the testing that you carried out. You should demonstrate that you have used a properly representative set of test cases to be confident that you have covered all the bases. Include details of the tests that you used and the rationale behind why they were chosen. Do not just reporduce the tests in your report without explanation.

Submit your code and report electronically in a single zip file called `ass3.zip` using the appropriate submission link on the COMP332 iLearn website by the due date and time. Your report must be in PDF format.

DO NOT SUBMIT YOUR ASSIGNMENT OR DOCUMENTATION IN ANY OTHER FORMAT THAN ZIP and PDF, RESPECTIVELY. Use of any other format slows down the marking and may result in a mark deduction.

### Marking ###

The assignment will be assessed according to the assessment standards for the unit learning outcomes.

Marks will be allocated equally to the code and to the report. Your code will be assessed for correctness and quality with respect to the assignment description. Marking of the report will assess the clarity and accuracy of your description and the adequacy of your testing. 20% of the marks for the assignment will be allocated to testing.

---
[Dominic Verity](http://orcid.org/0000-0002-4137-6982)  
Last modified: 22nd October 2018  
[Copyright (c) 2018 by Dominic Verity and Anthony Sloane. All rights reserved.](http://mozilla.org/MPL/2.0/)
