package decaf.backend.asm;

import decaf.lowlevel.Label;
import decaf.lowlevel.tac.TAC;

public class FuncLabel extends Label {

    public final TAC.Func func;

    public FuncLabel(String name, TAC.Func func) {
        super(name);
        this.func = func;
    }
}
