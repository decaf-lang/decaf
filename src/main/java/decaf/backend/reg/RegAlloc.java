package decaf.backend.reg;

import decaf.backend.asm.AsmEmitter;
import decaf.backend.asm.SubroutineInfo;
import decaf.backend.dataflow.CFG;
import decaf.lowlevel.instr.PseudoInstr;

/**
 * Register allocation.
 */
public abstract class RegAlloc {

    public RegAlloc(AsmEmitter emitter) {
        this.emitter = emitter;
    }

    /**
     * Entry of the main algorithm.
     *
     * @param graph control flow graph
     * @param info  basic info of the associated subroutine
     */
    public abstract void accept(CFG<PseudoInstr> graph, SubroutineInfo info);

    /**
     * Assembly emitter.
     */
    protected AsmEmitter emitter;
}
