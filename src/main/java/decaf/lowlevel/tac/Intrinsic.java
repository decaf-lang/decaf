package decaf.lowlevel.tac;

import decaf.lowlevel.label.IntrinsicLabel;

/**
 * Intrinsic procedure, a built-in function behave like "system calls".
 */
public final class Intrinsic {
    public enum Opcode {
        ALLOCATE, READ_LINE, READ_INT, STRING_EQUAL, PRINT_INT, PRINT_STRING, PRINT_BOOL, HALT
    }

    public final Opcode kind;

    /**
     * Name.
     */
    public final String name;

    /**
     * Label.
     */
    public final IntrinsicLabel entry;

    /**
     * Number of arguments.
     */
    public final int numArgs;

    /**
     * Allocate memory, exit if fails.
     * <p>
     * Argument: number of bytes to allocate.
     * Returns: the starting address of the allocated memory.
     */
    public static final Intrinsic ALLOCATE = new Intrinsic(Opcode.ALLOCATE, "_Alloc", 1);

    /**
     * Read a line from stdin and returns the string, maximum length 63.
     * <p>
     * Returns: the starting address of the string.
     */
    public static final Intrinsic READ_LINE = new Intrinsic(Opcode.READ_LINE, "_ReadLine", 0);

    /**
     * Read a 32-bit integer from stdin.
     * <p>
     * Returns: the integer.
     */
    public static final Intrinsic READ_INT = new Intrinsic(Opcode.READ_INT, "_ReadInteger", 0);

    /**
     * Compare two string and tell if their values are equal, i.e. they have the same length and characters are
     * pairwise equal.
     * <p>
     * Arguments: the starting addresses of the two strings.
     * Returns: 1 if they are equal, or 0 otherwise.
     */
    public static final Intrinsic STRING_EQUAL = new Intrinsic(Opcode.STRING_EQUAL, "_StringEqual", 2);

    /**
     * Print an integer to stdout.
     * <p>
     * Argument: the integer.
     */
    public static final Intrinsic PRINT_INT = new Intrinsic(Opcode.PRINT_INT, "_PrintInt", 1);

    /**
     * Print a string to stdout.
     * <p>
     * Argument: the starting address of the string.
     */
    public static final Intrinsic PRINT_STRING = new Intrinsic(Opcode.PRINT_STRING, "_PrintString", 1);

    /**
     * Print a boolean.
     * <p>
     * Argument: an integer representing the boolean, say 1 for true and 0 for false.
     */
    public static final Intrinsic PRINT_BOOL = new Intrinsic(Opcode.PRINT_BOOL, "_PrintBool", 1);

    /**
     * Halt/Exit.
     */
    public static final Intrinsic HALT = new Intrinsic(Opcode.HALT, "_Halt", 0);

    public static final Intrinsic[] ALL = {
            ALLOCATE, READ_LINE, READ_INT, STRING_EQUAL, PRINT_INT, PRINT_STRING, PRINT_BOOL, HALT
    };

    @Override
    public String toString() {
        return name;
    }

    private Intrinsic(Opcode kind, String name, int numArgs) {
        this.kind = kind;
        this.name = name;
        this.numArgs = numArgs;
        this.entry = new IntrinsicLabel(name, kind);
    }
}
