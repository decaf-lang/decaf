package decaf.backend.reg;

import decaf.backend.asm.AsmEmitter;
import decaf.backend.asm.SubroutineInfo;
import decaf.backend.dataflow.CFG;
import decaf.lowlevel.PseudoInstr;

import java.util.Random;

public abstract class RegAlloc {

    public RegAlloc(AsmEmitter emitter) {
        this.emitter = emitter;
    }

    public abstract void accept(CFG<PseudoInstr> graph, SubroutineInfo info);

    protected AsmEmitter emitter;

    protected Random random = new Random();
}
