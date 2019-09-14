package decaf.backend.asm;

import decaf.instr.NativeInstr;
import decaf.instr.Reg;
import decaf.instr.Temp;

public abstract class SubroutineEmitter {

    protected SubroutineEmitter(AsmEmitter emitter, SubroutineInfo info) {
        this.info = info;
        this.printer = emitter.printer;
    }

    public abstract void emitStoreToStack(Reg src);

    public abstract void emitLoadFromStack(Reg dst, Temp src);

    public abstract void emitMove(Reg dst, Reg src);

    public abstract void emitNative(NativeInstr item);

    public abstract void emitEnd();

    protected SubroutineInfo info;

    protected AsmCodePrinter printer;
}