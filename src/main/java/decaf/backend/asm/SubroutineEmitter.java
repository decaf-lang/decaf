package decaf.backend.asm;

import decaf.lowlevel.AsmCodePrinter;
import decaf.lowlevel.instr.NativeInstr;
import decaf.lowlevel.instr.Reg;
import decaf.lowlevel.instr.Temp;
import decaf.lowlevel.label.Label;

/**
 * Emit assembly code for a subroutine.
 */
public abstract class SubroutineEmitter {

    protected SubroutineEmitter(AsmEmitter emitter, SubroutineInfo info) {
        this.info = info;
        this.printer = emitter.printer;
    }

    /**
     * Append an assembly instruction that stores the value of a register to stack.
     *
     * @param src source register
     */
    public abstract void emitStoreToStack(Reg src);

    /**
     * Append an assembly instruction that loads a value from stack to a register.
     *
     * @param dst destination register
     * @param src source temp
     */
    public abstract void emitLoadFromStack(Reg dst, Temp src);

    /**
     * Append an assembly instruction that copies value between two registers.
     *
     * @param dst destination register
     * @param src source register
     */
    public abstract void emitMove(Reg dst, Reg src);

    /**
     * Append a given assembly instruction.
     *
     * @param instr assembly instruction
     */
    public abstract void emitNative(NativeInstr instr);

    /**
     * Append a label.
     *
     * @param label label
     */
    public abstract void emitLabel(Label label);

    /**
     * Call this when you have appended all user and synthetic (by register allocation algorithm) instructions of
     * this subroutine.
     */
    public abstract void emitEnd();

    /**
     * Basic info of this subroutine.
     */
    protected SubroutineInfo info;

    /**
     * Assembly code pretty printer.
     */
    protected AsmCodePrinter printer;
}