# The New Decaf Compiler

## Getting Started

This project requires JDK 12.

Other dependencies will be automatically downloaded from the maven central repository by
the build script.

## Build

Type the standard Gradle build in your CLI:

```
./gradlew build
```

The built jar will be located at `build/libs/decaf-1.0.jar`.

Or, import the project in a Java IDE (like IDEA or eclipse) and use gradle plugin if avaiable.

## Run

In your CLI, type

```
java -jar --enable-preview build/libs/decaf-1.0.jar -h
```

to display the usage help.

By default, the frontend targets do the following things:

- PA1: parse source code and output the pretty printed tree, or error messages
- PA2: type check and output the pretty printed scopes, or error messages
- PA3: generate TAC code, dump it to a .tac file, and then output the result of executing the tac 
code with our built-in simulator.
