package decaf.lowlevel;

import decaf.lowlevel.instr.NativeInstr;
import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.instr.Reg;
import decaf.lowlevel.instr.Temp;
import decaf.lowlevel.label.Label;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility registers and instructions of MIPS 32.
 */
public class Mips {

    // Registers

    public static final Reg ZERO = new Reg(0, "$zero"); // always zero (not allocatable)
    public static final Reg AT = new Reg(1, "$at"); // assembler reserved (not used by decaf)
    public static final Reg V0 = new Reg(2, "$v0"); // return value 0
    public static final Reg V1 = new Reg(3, "$v1"); // return value 1, but used as an additional temporary register
    public static final Reg A0 = new Reg(4, "$a0"); // arg 0
    public static final Reg A1 = new Reg(5, "$a1"); // arg 1
    public static final Reg A2 = new Reg(6, "$a2"); // arg 2
    public static final Reg A3 = new Reg(7, "$a3"); // arg 3
    public static final Reg T0 = new Reg(8, "$t0");
    public static final Reg T1 = new Reg(9, "$t1");
    public static final Reg T2 = new Reg(10, "$t2");
    public static final Reg T3 = new Reg(11, "$t3");
    public static final Reg T4 = new Reg(12, "$t4");
    public static final Reg T5 = new Reg(13, "$t5");
    public static final Reg T6 = new Reg(14, "$t6");
    public static final Reg T7 = new Reg(15, "$t7");
    public static final Reg S0 = new Reg(16, "$s0");
    public static final Reg S1 = new Reg(17, "$s1");
    public static final Reg S2 = new Reg(18, "$s2");
    public static final Reg S3 = new Reg(19, "$s3");
    public static final Reg S4 = new Reg(20, "$s4");
    public static final Reg S5 = new Reg(21, "$s5");
    public static final Reg S6 = new Reg(22, "$s6");
    public static final Reg S7 = new Reg(23, "$s7");
    public static final Reg T8 = new Reg(24, "$t8");
    public static final Reg T9 = new Reg(25, "$t9");
    public static final Reg K0 = new Reg(26, "$k0"); // kernel 0 (not used by decaf)
    public static final Reg K1 = new Reg(27, "$k1"); // kernel 1 (not used by decaf)
    public static final Reg GP = new Reg(28, "$gp"); // global pointer (not used by decaf)
    public static final Reg SP = new Reg(29, "$sp"); // stack pointer (not allocatable)
    public static final Reg S8 = new Reg(30, "$s8"); // also called $fp, but used as an additional saved register
    public static final Reg RA = new Reg(31, "$ra"); // return address

    public static final Reg[] callerSaved = new Reg[]{
            V1, T0, T1, T2, T3, T4, T5, T6, T7, T8, T9
    };

    public static final Reg[] calleeSaved = new Reg[]{
            S0, S1, S2, S3, S4, S5, S6, S7, S8
    };

    public static final Reg[] allocatableRegs = ArrayUtils.addAll(callerSaved, calleeSaved);

    public static final Reg[] argRegs = new Reg[]{
            A0, A1, A2, A3
    };

    // Instructions

    private static final String FMT1 = "%s";
    private static final String FMT2 = "%s, %s";
    private static final String FMT3 = "%s, %s, %s";
    private static final String FMT_OFFSET = "%s, %d(%s)";

    private static String format(String op, String fmt, Object... args) {
        var sb = new StringBuilder();
        sb.append(op).append(' ');
        for (var i = 0; i < 7 - op.length(); i++) {
            sb.append(' ');
        }
        sb.append(String.format(fmt, args));
        return sb.toString();
    }

    public static class Move extends PseudoInstr {

        public Move(Temp dst, Temp src) {
            super(new Temp[]{dst}, new Temp[]{src});
        }

        @Override
        public String toString() {
            return format("move", FMT2, dsts[0], srcs[0]);
        }
    }

    public enum UnaryOp {
        NEG, NOT
    }

    public static class Unary extends PseudoInstr {

        public Unary(UnaryOp op, Temp dst, Temp src) {
            super(new Temp[]{dst}, new Temp[]{src});
            this.op = op.toString().toLowerCase();
        }

        private String op;

        @Override
        public String toString() {
            return format(op, FMT2, dsts[0], srcs[0]);
        }
    }

    public enum BinaryOp {
        ADD, SUB, MUL, DIV, REM,
        SGT, SGE, SEQ, SNE, SLE, SLT,
        AND, OR
    }

    public static class Binary extends PseudoInstr {

        public Binary(BinaryOp op, Temp dst, Temp src0, Temp src1) {
            super(new Temp[]{dst}, new Temp[]{src0, src1});
            this.op = op.toString().toLowerCase();
        }

        private String op;

        @Override
        public String toString() {
            return format(op, FMT3, dsts[0], srcs[0], srcs[1]);
        }
    }

    public enum BranchOp {
        BEQZ, BNEZ
    }

    public static class Branch extends PseudoInstr {

        public Branch(BranchOp op, Temp src, Label to) {
            super(Kind.COND_JMP, new Temp[]{}, new Temp[]{src}, to);
            this.op = op.toString().toLowerCase();
        }

        private String op;

        @Override
        public String toString() {
            return format(op, FMT2, srcs[0], label);
        }
    }

    public static class Jump extends PseudoInstr {

        public Jump(Label to) {
            super(Kind.JMP, new Temp[]{}, new Temp[]{}, to);
        }

        @Override
        public String toString() {
            return format("j", FMT1, label);
        }
    }

    /**
     * The special jump-to-epilogue instruction {@code j epilogue} is regarded as a return statement.
     */
    public static class JumpToEpilogue extends PseudoInstr {

        public JumpToEpilogue(Label label) {
            super(Kind.RET, new Temp[]{}, new Temp[]{}, new Label(label + EPILOGUE_SUFFIX));
        }

        @Override
        public String toString() {
            return format("j", FMT1, label);
        }
    }

    public static class JumpAndLink extends PseudoInstr {

        public JumpAndLink(Label to) {
            super(Kind.SEQ, new Temp[]{}, new Temp[]{}, to);
        }

        @Override
        public String toString() {
            return format("jal", FMT1, label);
        }
    }

    public static class JumpAndLinkReg extends PseudoInstr {

        public JumpAndLinkReg(Temp src) {
            super(new Temp[]{}, new Temp[]{src});
        }

        @Override
        public String toString() {
            return format("jalr", FMT1, srcs[0]);
        }
    }

    public static class LoadWord extends PseudoInstr {

        public LoadWord(Temp dst, Temp base, int offset) {
            super(new Temp[]{dst}, new Temp[]{base});
            this.offset = offset;
        }

        private int offset;

        @Override
        public String toString() {
            return format("lw", FMT_OFFSET, dsts[0], offset, srcs[0]);
        }
    }

    public static class StoreWord extends PseudoInstr {

        public StoreWord(Temp src, Temp base, int offset) {
            super(new Temp[]{}, new Temp[]{src, base});
            this.offset = offset;
        }

        private int offset;

        @Override
        public String toString() {
            return format("sw", FMT_OFFSET, srcs[0], offset, srcs[1]);
        }
    }

    public static class LoadImm extends PseudoInstr {

        public LoadImm(Temp dst, int value) {
            super(new Temp[]{dst}, new Temp[]{});
            this.value = value;
        }

        private int value;

        @Override
        public String toString() {
            return format("li", FMT2, dsts[0], value);
        }
    }

    public static class LoadAddr extends PseudoInstr {

        public LoadAddr(Temp dst, Label label) {
            super(Kind.SEQ, new Temp[]{dst}, new Temp[]{}, label);
        }

        @Override
        public String toString() {
            return format("la", FMT2, dsts[0], label);
        }
    }

    public static class MipsLabel extends PseudoInstr {

        public MipsLabel(Label label) {
            super(label);
        }

        @Override
        public String toString() {
            return String.format("%s:", label);
        }
    }

    public static class Syscall extends NativeInstr {

        public Syscall() {
            super(new Reg[]{}, new Reg[]{});
        }

        @Override
        public String toString() {
            return "syscall";
        }
    }

    public static class NativeMove extends NativeInstr {

        public NativeMove(Reg dst, Reg src) {
            super(new Reg[]{dst}, new Reg[]{src});
        }

        @Override
        public String toString() {
            return format("move", FMT2, dsts[0], srcs[0]);
        }
    }

    public static class NativeLoadWord extends NativeInstr {

        public NativeLoadWord(Reg dst, Reg base, int offset) {
            super(new Reg[]{dst}, new Reg[]{base});
            this.offset = offset;
        }

        private int offset;

        @Override
        public String toString() {
            return format("lw", FMT_OFFSET, dsts[0], offset, srcs[0]);
        }
    }

    public static class NativeStoreWord extends NativeInstr {

        public NativeStoreWord(Reg src, Reg base, int offset) {
            super(new Reg[]{}, new Reg[]{src, base});
            this.offset = offset;
        }

        private int offset;

        @Override
        public String toString() {
            return format("sw", FMT_OFFSET, srcs[0], offset, srcs[1]);
        }
    }

    /**
     * Since the only possible usage of the {@code jr} is to return a subroutine with {@code jr $ra}.
     * Why not simply call this "Return"?
     */
    public static class NativeReturn extends NativeInstr {

        public NativeReturn() {
            super(Kind.RET, new Reg[]{RA}, new Reg[]{}, null);
        }

        @Override
        public String toString() {
            return format("jr", FMT1, RA);
        }
    }

    public static class SPAdd extends NativeInstr {

        public SPAdd(int offset) {
            super(new Reg[]{SP}, new Reg[]{SP});
            this.offset = offset;
        }

        private int offset;

        @Override
        public String toString() {
            return format("addiu", FMT3, dsts[0], srcs[0], offset);
        }
    }

    public static final String STR_PREFIX = "_S";

    public static final String EPILOGUE_SUFFIX = "_exit";
}
