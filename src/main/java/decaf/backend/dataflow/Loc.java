package decaf.backend.dataflow;

import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.instr.Temp;

import java.util.Set;

/**
 * A program location in a basic block, i.e. instruction with results of liveness analysis.
 */
public class Loc<I extends PseudoInstr> {
    public final I instr;
    public Set<Temp> liveIn;
    public Set<Temp> liveOut;

    Loc(I instr) {
        this.instr = instr;
    }
}
