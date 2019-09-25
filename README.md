# The New Decaf Compiler

Decaf is a Java-like, but much smaller programming language mainly for educational purpose.
We now have at least three different implementations of the compiler in Java, Scala and Rust.
Since the standard language has quite a limited set of language features, students are welcome to add their own new features.

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
- PA1-LL: like PA1, but use hand-coded LL parsing algorithm, with the help of a LL table generator [ll1pg](https://github.com/paulzfm/ll1pg)
- PA2: type check and output the pretty printed scopes, or error messages
- PA3: generate TAC (three-address code), dump it to a .tac file, and then output the execution result using our built-in simulator
- PA4: currently same with PA3, will be reserved for students to do a bunch of optimizations on TAC
- PA5: (default target) allocate registers and emit assembly code, currently we are using a very brute-force algorithm and only generates MIPS assembly code (with pseudo-ops, and no delayed branches)

To run the MIPS assembly code, you may need [spim](http://spimsimulator.sourceforge.net), a MIPS32 simulator.
For Mac OS users, simply install `spim` with `brew install spim` and run with `spim -file your_file.s`.

## Releases

See https://github.com/decaf-lang/decaf/releases for releases, including separate frameworks for PA1 -- PA3.

## Materials

We have a couple of Chinese documents on the language specification and implementation outlines:

- https://decaf-lang.gitbook.io
- https://decaf-project.gitbook.io

## Development & Contribution

In future, we will develop on (possibly variates of) development branches,
and only merge release versions into the master branch.

Issues and pull requests for fixing bugs are welcome. However, adding new language features will not be considered, because that's students' work!
