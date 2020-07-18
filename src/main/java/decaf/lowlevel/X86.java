package decaf.lowlevel;

import decaf.lowlevel.instr.*;
import decaf.lowlevel.label.Label;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility registers and instructions of X86-64.
 */
public class X86 {
    // Registers
    public static final Reg EAX = new Reg(0, "%eax");
    public static final Reg EBX = new Reg(1, "%ebx");
    public static final Reg ECX = new Reg(2, "%ecx");
    public static final Reg EDX = new Reg(3, "%edx");
    public static final Reg EDI = new Reg(4, "%edi");
    public static final Reg ESI = new Reg(5, "%esi");
    public static final Reg EBP = new Reg(6, "%ebp");
    public static final Reg ESP = new Reg(7, "%esp");

    public static final Reg[] callerSaved = new Reg[]{
            // We do no include EAX because
            // restoreCallerSave() would clashes the return value in EAX
            ECX, EDX
    };

    public static final Reg[] calleeSaved = new Reg[]{
            EBX, EDI, ESI
    };

    public static final Reg[] allocatableRegs = ArrayUtils.addAll(callerSaved, calleeSaved);

    public static final Reg[] argRegs = new Reg[]{};

    // Instructions
    private static String literalsEscaped(Object s) {
        return (s instanceof Integer) ? String.format("$%s", s) : String.format("%s", s);
    }
    private static String formatUnary(String opString, Object operand) {
        return String.format("%-8s %s", opString, literalsEscaped(operand));
    }
    private static String formatBinary(String opString, Object op2, Object dstOp) {
        return String.format("%-8s %s, %s", opString, literalsEscaped(op2), dstOp);
    }
    private static String formatCompare(String opString, Object lhs, Object rhs) {
        return String.format("%-8s %s, %s", opString, literalsEscaped(lhs), literalsEscaped(rhs));
    }
    private static String formatTernary(String opString, Object dst, Object lhs, Object rhs) {
        return String.format("%-8s %s, %s, %s", opString, lhs, rhs, dst);
    }
    private static String formaIndirectCall(Object to) {
        return String.format("%-8s *%s", "call", literalsEscaped(to));
    }
    private static String formatLoadWord(Object base, int offset, Object dst) {
        return String.format("%-8s %d(%s), %s", "movl", offset, base, dst);
    }
    private static String formatStoreWord(Object src, Object base, int offset) {
        return String.format("%-8s %s, %d(%s)", "movl", src, offset, base);
    }

    public static class Move extends PseudoInstr {

        public Move(Temp dst, Temp src) {
            super(new Temp[]{dst}, new Temp[]{src});
        }

        @Override
        public String toString() {
            return formatBinary("mov", srcs[0], dsts[0]);
        }
    }

    public static class Push extends PseudoInstr {

        public Push(Temp operand) {
            super(new Temp[]{}, new Temp[]{operand});
        }

        @Override
        public String toString() {
            return formatUnary("push", srcs[0]);
        }
    }

    public static class Pop extends PseudoInstr {

        public Pop(Temp operand) {
            super(new Temp[]{operand}, new Temp[]{});
        }

        @Override
        public String toString() {
            return formatUnary("pop", dsts[0]);
        }
    }

    public enum UnaryOp {
        NEG, NOT
    }

    public static class Unary extends PseudoInstr {

        public Unary(UnaryOp op, Temp operand) {
            super(new Temp[]{operand}, new Temp[]{operand});
            this.op = op.toString().toLowerCase();
        }

        private String op;

        @Override
        public String toString() {
            return formatUnary(op, srcs[0]);
        }
    }

    public enum BinaryOp {
        ADD, SUB, IMULL, REM, DIV,
        AND, OR, CMP
    }

    public static class Binary extends PseudoInstr {

        public Binary(BinaryOp op, Temp dst, Temp src) {
            super(new Temp[]{dst}, new Temp[]{src, dst});
            this.op = op.toString().toLowerCase();
        }

        private String op;

        @Override
        public String toString() {
            return formatBinary(op, srcs[0], dsts[0]);
        }
    }

    public static class Compare extends PseudoInstr {

        public Compare(Temp lhs, Temp rhs) {
            super(new Temp[]{}, new Temp[]{lhs, rhs});
        }

        @Override
        public String toString() {
            return formatBinary("cmp", srcs[0], srcs[1]);
        }
    }

    public static class CompareZero extends PseudoInstr {

        public CompareZero(Temp lhs) {
            super(new Temp[]{}, new Temp[]{lhs});
        }

        @Override
        public String toString() {
            return formatCompare("cmp", srcs[0], 0);
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
            return formatLoadWord(srcs[0], offset, dsts[0]);
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
            return formatStoreWord(srcs[0], srcs[1], offset);
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
            return formatBinary("mov", value, dsts[0]);
        }
    }

    public static class LoadAddr extends PseudoInstr {

        public LoadAddr(Temp dst, Label label) {
            super(Kind.SEQ, new Temp[]{dst}, new Temp[]{}, label);
        }

        @Override
        public String toString() {
            return formatBinary("lea", label, dsts[0]);
        }
    }

    public static class X86Label extends PseudoInstr {

        public X86Label(Label label) {
            super(label);
        }

        @Override
        public String toString() {
            return String.format("%s:", label);
        }
    }

    public static class X86Call extends PseudoInstr {

        public X86Call(Label to) {
            super(Kind.SEQ, new Temp[]{}, new Temp[]{}, to);
        }

        @Override
        public String toString() {
            return formatUnary("call", label);
        }
    }

    public static class X86IndirectCall extends PseudoInstr {

        public X86IndirectCall(Temp to) {
            super(new Temp[]{}, new Temp[]{to});
        }

        @Override
        public String toString() {
            return formaIndirectCall(srcs[0]);
        }
    }

    public static class Jump extends PseudoInstr {

        public Jump(Label to) {
            super(Kind.JMP, new Temp[]{}, new Temp[]{}, to);
        }

        @Override
        public String toString() {
            return formatUnary("jmp", label);
        }
    }

    public static class JumpToEpilogue extends PseudoInstr {

        public JumpToEpilogue(Label label) {
            super(Kind.RET, new Temp[]{}, new Temp[]{}, new Label(label + EPILOGUE_SUFFIX));
        }

        @Override
        public String toString() {
            return formatUnary("jmp", label);
        }
    }


    public enum CondJumpOp {
        JE, JNE
    }

    public static class CondJump extends HoleInstr {

        public CondJump(CondJumpOp op, Temp cond, Label to) {
            super(Kind.COND_JMP, new Temp[]{}, new Temp[]{cond}, to);
            this.op = op.toString().toLowerCase();
        }

        private final String op;

        @Override
        public String toString() {
            return formatUnary(op, label);
        }
    }

    public enum SignedIntDivRemOp {
        DIV, REM
    }

    public static class SignedIntDivRem extends HoleInstr {

        public SignedIntDivRem(SignedIntDivRemOp op, Temp dst, Temp lhs, Temp rhs) {
            super(new Temp[]{dst}, new Temp[]{lhs, rhs});
            this.op = op;
        }

        public final SignedIntDivRemOp op;

        @Override
        public String toString() {
            return formatTernary(op.toString().toLowerCase(), dsts[0], srcs[0], srcs[1]);
        }
    }

    public enum SetCCOp {
        SETE, SETNE, SETL, SETLE, SETG, SETGE, ERR
    }

    public static class SetCC extends HoleInstr {

        public SetCCOp op;

        public SetCC(SetCCOp op, Temp dst) {
            super(new Temp[]{dst}, new Temp[]{});
            this.op = op;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", op, dsts[0]);
        }
    }

    public abstract static class SignedIntDivRemNative extends NativeInstr {
        public final SignedIntDivRemOp op;
        public SignedIntDivRemNative(SignedIntDivRemOp op, Reg[] dsts, Reg[] srcs) {
            super(dsts, srcs);
            this.op = op;
        }
    }

    // Sign extend EAX into EDX:EAX.
    public static class NativeCLTD extends NativeInstr {

        public NativeCLTD() {
            super(new Reg[]{EAX, EDX}, new Reg[]{EAX});
        }

        @Override
        public String toString() {
            return "cltd";
        }
    }

    public static class NativeDivRem extends NativeInstr {

        public NativeDivRem(Reg r) {
            super(new Reg[]{EAX, EDX}, new Reg[]{r});
        }

        @Override
        public String toString() {
            return formatUnary("idivl", srcs[0]);
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

    public static class NativeCompareToZero extends NativeInstr {

        public NativeCompareToZero(Reg r) {
            super(new Reg[]{}, new Reg[]{r});
        }

        @Override
        public String toString() {
            return formatBinary("cmp", "$0", srcs[0]);
        }
    }

    public static class NativeMove extends NativeInstr {

        public NativeMove(Reg dst, Reg src) {
            super(new Reg[]{dst}, new Reg[]{src});
        }

        @Override
        public String toString() {
            return formatBinary("mov", srcs[0], dsts[0]);
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
            return formatLoadWord(srcs[0], offset, dsts[0]);
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
            return formatStoreWord(srcs[0], srcs[1], offset);
        }
    }

    public static class NativeReturn extends NativeInstr {

        public NativeReturn() {
            super(Kind.RET, new Reg[]{}, new Reg[]{}, null);
        }

        @Override
        public String toString() {
            return "ret";
        }
    }

    public static class NativeLeave extends NativeInstr {

        public NativeLeave() {
            super(Kind.RET, new Reg[]{}, new Reg[]{}, null);
        }

        @Override
        public String toString() {
            return "leave";
        }
    }

    public static class RSPAdd extends NativeInstr {

        public RSPAdd(int offset) {
            super(new Reg[]{ESP}, new Reg[]{ESP});
            this.offset = offset;
        }

        private int offset;

        @Override
        public String toString() {
            return formatBinary("add", offset, dsts[0]);
        }
    }

    public static class NativePush extends NativeInstr {

        public NativePush(Reg r) {
            super(new Reg[]{}, new Reg[]{r});
        }

        @Override
        public String toString() {
            return formatUnary("push", srcs[0]);
        }
    }

    private static String lower8bits(Reg r) {
        if (r.equals(EAX)) {
            return "%al";
        } else if (r.equals(EBX)) {
            return "%bl";
        } else if (r.equals(ECX)) {
            return "%cl";
        } else if (r.equals(EDX)) {
            return "%dl";
        } else {
            assert false;
            return "";
        }
    }

    public enum ExtendOp {
        MOVZBL
    }

    public static class ExtendFrom8Bit extends NativeInstr {

        public ExtendFrom8Bit(ExtendOp op, Reg dst, Reg src) {
            super(new Reg[]{dst}, new Reg[]{src});
            this.op = op.toString().toLowerCase();
        }

        private String op;

        @Override
        public String toString() {
            return formatBinary(op, lower8bits((Reg) srcs[0]), dsts[0]);
        }
    }

    public static class NativeSetCC extends NativeInstr {
        public NativeSetCC(SetCCOp op, Reg dst) {
            super(new Reg[]{dst}, new Reg[]{});
            this.op = op.toString().toLowerCase();
        }

        private String op;

        @Override
        public String toString() {
            return formatUnary(op, lower8bits((Reg) dsts[0]));
        }
    }

    public static class NativePop extends NativeInstr {

        public NativePop(Reg r) {
            super(new Reg[]{r}, new Reg[]{});
        }

        @Override
        public String toString() {
            return formatUnary("pop", dsts[0]);
        }
    }

    public static final String STR_PREFIX = "_S";

    public static final String EPILOGUE_SUFFIX = "_exit";
}
