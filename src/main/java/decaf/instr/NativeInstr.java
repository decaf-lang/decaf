package decaf.instr;

public abstract class NativeInstr extends PseudoInstr {

    public NativeInstr(Kind kind, String op, Reg[] dsts, Reg[] srcs, Label jumpTo, Object... imms) {
        super(kind, op, dsts, srcs, jumpTo, imms);
    }

    public NativeInstr(String op, Reg[] dsts, Reg[] srcs, Object... imms) {
        super(Kind.SEQ, op, dsts, srcs, null, imms);
    }
}
