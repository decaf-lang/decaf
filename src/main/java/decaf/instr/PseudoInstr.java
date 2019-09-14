package decaf.instr;

public abstract class PseudoInstr extends InstrLike {

    public PseudoInstr(Kind kind, String op, Temp[] dsts, Temp[] srcs, Label jumpTo, Object... imms) {
        super(kind, op, dsts, srcs, jumpTo, imms);
    }

    public PseudoInstr(String op, Temp[] dsts, Temp[] srcs, Object... imms) {
        super(Kind.SEQ, op, dsts, srcs, null, imms);
    }

    public PseudoInstr(Label label) {
        super(label);
    }

    public NativeInstr toNative(Reg[] dstRegs, Reg[] srcRegs) {
        var oldDsts = this.dsts;
        var oldSrcs = this.srcs;

        this.dsts = dstRegs;
        this.srcs = srcRegs;
        var format = getFormat();
        var args = getArgs();
        var nt = new NativeInstr(kind, op, dstRegs, srcRegs, jumpTo, imms) {
            @Override
            public String getFormat() {
                return format;
            }

            @Override
            public Object[] getArgs() {
                return args;
            }
        };

        this.dsts = oldDsts;
        this.srcs = oldSrcs;
        return nt;
    }

    public boolean isTodo() {
        return false;
    }
}
