package decaf.backend.asm;

import decaf.lowlevel.AsmCodePrinter;
import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.instr.Reg;
import decaf.lowlevel.tac.TacFunc;
import decaf.lowlevel.tac.VTable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Emit assembly code.
 */
public abstract class AsmEmitter {

    /**
     * Target platform name.
     */
    public final String platformName;

    /**
     * Platform-specific registers accounted for register allocation, i.e. general registers.
     */
    public final Reg[] allocatableRegs;

    /**
     * Platform-specific registers that need be saved by caller.
     */
    public final Reg[] callerSaveRegs;

    public AsmEmitter(String platformName, Reg[] allocatableRegs, Reg[] callerSaveRegs) {
        this.platformName = platformName;
        this.allocatableRegs = allocatableRegs;
        this.callerSaveRegs = callerSaveRegs;
    }

    /**
     * Emit assembly code for a virtual table.
     *
     * @param vtbl virtual table
     */
    public abstract void emitVTable(VTable vtbl);

    /**
     * Instruction selection for a TAC function.
     * <p>
     * Since no register allocation is done, the generated instructions may still contain pseudo registers (temps).
     *
     * @param func TAC function
     * @return a pair of the instruction sequence, and the basic info of the function
     */
    public abstract Pair<List<PseudoInstr>, SubroutineInfo> selectInstr(TacFunc func);

    /**
     * Call this when all virtual tables are done, and you want to emit code for subroutines.
     */
    public abstract void emitSubroutineBegin();

    /**
     * Begin to emit code for a subroutine.
     *
     * @param info basic info of this subroutine
     * @return emitter of this subroutine
     */
    public abstract SubroutineEmitter emitSubroutine(SubroutineInfo info);

    /**
     * Call this when all subroutines are done, and you want to finish.
     *
     * @return string representation of the emitted assembly code
     */
    public abstract String emitEnd();

    /**
     * Assembly code pretty printer.
     */
    protected final AsmCodePrinter printer = new AsmCodePrinter();
}
