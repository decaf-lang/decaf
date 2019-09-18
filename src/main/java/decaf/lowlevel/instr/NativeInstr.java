package decaf.lowlevel.instr;

import decaf.lowlevel.label.Label;

/**
 * A native instruction.
 * <p>
 * Saying "native", we mean every operand might be a native register.
 * Note that it is always safe to cast a native instruction to a pseudo one, but NOT in reverse.
 */
public abstract class NativeInstr extends PseudoInstr {
    /**
     * Similar to {@link PseudoInstr#PseudoInstr(Kind, Temp[], Temp[], Label)}.
     */
    public NativeInstr(Kind kind, Reg[] dsts, Reg[] srcs, Label label) {
        super(kind, dsts, srcs, label);
    }

    /**
     * Similar to {@link PseudoInstr#PseudoInstr(Temp[], Temp[])}.
     */
    public NativeInstr(Reg[] dsts, Reg[] srcs) {
        super(Kind.SEQ, dsts, srcs, null);
    }

    /**
     * Similar to {@link PseudoInstr#PseudoInstr(Label)}.
     */
    public NativeInstr(Label label) {
        super(Kind.LABEL, new Temp[]{}, new Temp[]{}, label);
    }
}
