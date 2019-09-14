package decaf.backend.asm;

import decaf.instr.PseudoInstr;
import decaf.instr.Reg;
import decaf.instr.tac.TAC;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public abstract class AsmEmitter {

    public final String platformName;

    public final Reg[] allocatableRegs;

    public final Reg[] callerSaveRegs;

    public AsmEmitter(String platformName, Reg[] allocatableRegs, Reg[] callerSaveRegs) {
        this.platformName = platformName;
        this.allocatableRegs = allocatableRegs;
        this.callerSaveRegs = callerSaveRegs;
    }

    public abstract void emitVTable(TAC.VTable vtbl);

    public abstract Pair<List<PseudoInstr>, SubroutineInfo> selectInstr(TAC.Func func);

    public abstract void emitSubroutineBegin();

    public abstract SubroutineEmitter emitSubroutine(SubroutineInfo info);

    public abstract String emitEnd();

    protected final AsmCodePrinter printer = new AsmCodePrinter();
}
