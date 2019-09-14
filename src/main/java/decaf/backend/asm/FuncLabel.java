package decaf.backend.asm;

import decaf.instr.Label;
import decaf.instr.tac.TAC;

public class FuncLabel extends Label {

    public final TAC.Func func;

    public FuncLabel(String name, TAC.Func func) {
        super(name);
        this.func = func;
    }
}
