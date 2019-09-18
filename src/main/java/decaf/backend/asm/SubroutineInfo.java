package decaf.backend.asm;

import decaf.lowlevel.label.FuncLabel;

/**
 * Basic info of subroutine.
 */
public class SubroutineInfo {
    /**
     * Label of the function entry.
     */
    public final FuncLabel funcLabel;

    /**
     * Number of arguments.
     */
    public final int numArg;

    /**
     * Does this subroutine call others?
     */
    public final boolean hasCalls;

    /**
     * Max. stack size needed to store arguments.
     */
    public final int argsSize;

    public SubroutineInfo(FuncLabel funcLabel, int numArg, boolean hasCalls, int argsSize) {
        this.funcLabel = funcLabel;
        this.numArg = numArg;
        this.hasCalls = hasCalls;
        this.argsSize = argsSize;
    }
}