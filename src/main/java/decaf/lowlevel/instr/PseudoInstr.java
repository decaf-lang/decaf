package decaf.lowlevel.instr;

import decaf.lowlevel.label.Label;

import java.util.Arrays;
import java.util.List;

/**
 * A pseudo instruction.
 * <p>
 * Saying "pseudo", we mean every operand might be a pseudo register, i.e. temp.
 */
public abstract class PseudoInstr {
    /**
     * Instruction kind.
     * <ul>
     *     <li>{@code LABEL:} a label, not a "real" instruction that is executable</li>
     *     <li>{@code SEQ:} a sequential instruction, say after executing this, the physically "next" instruction will
     *     be consecutively executed</li>
     *     <li>{@code JMP:} a jump instruction</li>
     *     <li>{@code COND_JMP:} a conditional jump instruction</li>
     *     <li>{@code RET:} a return instruction</li>
     * </ul>
     */
    public enum Kind {
        LABEL, SEQ, JMP, COND_JMP, RET
    }

    public final Kind kind;

    /**
     * Destination operands, i.e. written temps.
     */
    public Temp[] dsts;

    /**
     * Source operands, i.e. read temps.
     */
    public Temp[] srcs;

    /**
     * Label.
     * <p>
     * When the instruction is {@link Kind#LABEL}, this is it.
     * When the instruction is {@link Kind#JMP} or {@link Kind#COND_JMP}, this is the target it may jump to.
     */
    public final Label label;

    public PseudoInstr(Kind kind, Temp[] dsts, Temp[] srcs, Label label) {
        this.kind = kind;
        this.dsts = dsts;
        this.srcs = srcs;
        this.label = label;
    }

    /**
     * A special constructor to create a {@link Kind#SEQ}.
     *
     * @param dsts destination operands
     * @param srcs source operands
     */
    public PseudoInstr(Temp[] dsts, Temp[] srcs) {
        this(Kind.SEQ, dsts, srcs, null);
    }

    /**
     * A special constructor to create a {@link Kind#LABEL}.
     *
     * @param label the label
     */
    public PseudoInstr(Label label) {
        this(Kind.LABEL, new Temp[]{}, new Temp[]{}, label);
    }

    /**
     * Get read temps.
     *
     * @return read temps
     */
    public List<Temp> getRead() {
        return Arrays.asList(srcs);
    }

    /**
     * Get written temps.
     *
     * @return written temps
     */
    public List<Temp> getWritten() {
        return Arrays.asList(dsts);
    }

    /**
     * Transform into a {@link NativeInstr}, by replacing all pseudo registers with native registers.
     *
     * @param dstRegs destination registers
     * @param srcRegs source registers
     * @return native instruction
     */
    public NativeInstr toNative(Reg[] dstRegs, Reg[] srcRegs) {
        var oldDsts = this.dsts;
        var oldSrcs = this.srcs;

        this.dsts = dstRegs;
        this.srcs = srcRegs;
        var str = toString();
        var nativeInstr = new NativeInstr(kind, dstRegs, srcRegs, label) {
            @Override
            public String toString() {
                return str;
            }
        };

        this.dsts = oldDsts;
        this.srcs = oldSrcs;
        return nativeInstr;
    }

    public boolean isLabel() {
        return kind.equals(Kind.LABEL);
    }

    public boolean isSequential() {
        return kind.equals(Kind.SEQ);
    }

    public boolean isReturn() {
        return kind.equals(Kind.RET);
    }

    public abstract String toString();
}