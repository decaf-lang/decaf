package decaf.lowlevel;

import java.util.Arrays;
import java.util.List;

/**
 * Something that looks like an instruction.
 */
public abstract class InstrLike {
    public enum Kind {
        LABEL, SEQ, JMP, COND_JMP, RET
    }

    public final Kind kind;

    public final String op;

    public Temp[] dsts;

    public Temp[] srcs;

    public final Label jumpTo;

    public final Object[] imms;

    public InstrLike(Kind kind, String op, Temp[] dsts, Temp[] srcs, Label jumpTo, Object... imms) {
        this.kind = kind;
        this.op = op;
        this.dsts = dsts;
        this.srcs = srcs;
        this.jumpTo = jumpTo;
        this.imms = imms;
    }

    public InstrLike(Label label) {
        this(Kind.LABEL, "", new Temp[]{}, new Temp[]{}, label);
    }

    public List<Temp> getRead() {
        return Arrays.asList(srcs);
    }

    public List<Temp> getWritten() {
        return Arrays.asList(dsts);
    }

    // TODO: move format to subclasses

    public abstract String getFormat();

    public abstract Object[] getArgs();

    protected int getPadding() {
        return 6;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(op);
        for (int i = 0; i < getPadding() - op.length(); i++) {
            sb.append(' ');
        }
        sb.append(String.format(getFormat(), getArgs()));
        return sb.toString();
    }

    public boolean isSequential() {
        return kind.equals(Kind.SEQ);
    }

    public boolean isLabel() {
        return kind.equals(Kind.LABEL);
    }
}