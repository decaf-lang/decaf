package decaf.backend.asm;

public class SubroutineInfo {
    public final FuncLabel funcLabel;
    public final int numArg;
    public final boolean hasCalls;
    public final int argsSize;

    public SubroutineInfo(FuncLabel funcLabel, int numArg, boolean hasCalls, int argsSize) {
        this.funcLabel = funcLabel;
        this.numArg = numArg;
        this.hasCalls = hasCalls;
        this.argsSize = argsSize;
    }
}