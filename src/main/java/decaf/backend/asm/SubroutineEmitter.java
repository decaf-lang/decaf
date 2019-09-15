package decaf.backend.asm;

import decaf.lowlevel.Label;
import decaf.lowlevel.NativeInstr;
import decaf.lowlevel.Reg;
import decaf.lowlevel.Temp;

public abstract class SubroutineEmitter {

    protected SubroutineEmitter(AsmEmitter emitter, SubroutineInfo info) {
        this.info = info;
        this.printer = emitter.printer;
    }

    public abstract void emitStoreToStack(Reg src);

    public abstract void emitLoadFromStack(Reg dst, Temp src);

    public abstract void emitMove(Reg dst, Reg src);

    public abstract void emitNative(NativeInstr item);

    public abstract void emitLabel(Label label);

    public abstract void emitEnd();

    protected SubroutineInfo info;

    protected AsmCodePrinter printer;
}