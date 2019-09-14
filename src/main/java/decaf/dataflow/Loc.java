package decaf.dataflow;

import decaf.instr.InstrLike;
import decaf.instr.Temp;

import java.util.Set;

/**
 * A program location in a basic block.
 *
 * Saying a "location", this stands for not only the tac instruction at that location, but also the "state" of program
 * execution. The "state" is reflected by a couple of results produced by preforming program analysis, e.g. the
 * built-in liveness analysis.
 */
public class Loc<I extends InstrLike> {
    public final I instr;
    public Set<Temp> liveIn;
    public Set<Temp> liveOut;

    public Loc(I instr) {
        this.instr = instr;
    }

    @Override
    public String toString() {
        return instr.toString() + "  " + BasicBlock.setToString(liveOut);
    }
}
