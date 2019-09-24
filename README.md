# The New Decaf Compiler

Decaf is a Java-like, but much smaller programming language mainly for educational purpose.
We now have at least three different implementations of the compiler in Java, Scala and Rust.
Since the standard language has quite a limited set of language features, students are welcome to add their own new features.

We are happy to announce that this new compiler, written in Java, is now tested upon a partial set of Decaf programs, from parsing to emitting MIPS assembly code. However, a lot of work will be done in a couple of weeks -- refactoring, documenting and revising to meet the new Decaf language standard. More materials are coming soon.

The final version for the compiler course will be ready right before the publication of the first PA assignment.

## Getting Started

This project requires JDK 12.

Other dependencies will be automatically downloaded from the maven central repository by the build script.

## Build

Type the standard Gradle build command in your CLI:

```sh
./gradlew build
```

The built jar will be located at `build/libs/decaf.jar`.

Or, import the project in a Java IDE (like IDEA or Eclipse, or your favorite VS Code) and use gradle plugin, if available.

## Run

In your CLI, type

```sh
java -jar --enable-preview build/libs/decaf.jar -h
```

to display the usage help.

Possible targets/tasks are:

- PA1: parse source code and output the pretty printed tree, or error messages
- PA2: type check and output the pretty printed scopes, or error messages
- PA3: generate TAC (three-address code), dump it to a .tac file, and then output the execution result using our built-in simulator
- PA4: currently same with PA3, will be reserved for students to do a bunch of optimizations on TAC
- PA5: (default target) allocate registers and emit assembly code, currently we are using a very brute-force algorithm and only generates MIPS assembly code (with pseudo-ops, and no delayed branches)

To run the MIPS assembly code, you may need [spim](http://spimsimulator.sourceforge.net), a MIPS32 simulator.
For Mac OS users, simply install `spim` with `brew install spim` and run with `spim -file your_file.s`.
