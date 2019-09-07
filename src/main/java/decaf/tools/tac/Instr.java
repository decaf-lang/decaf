package decaf.tools.tac;

import java.util.Objects;
import java.util.Optional;

import static decaf.utils.MiscUtils.quote;

public abstract class Instr {

    int opcode;

    public Boolean isPseudo() {
        return false;
    }

    public Boolean isMark() {
        return false;
    }

    public Boolean isReturn() {
        return false;
    }

    public abstract void accept(Visitor v);

    public abstract String toString();

    public static class Assign extends Instr {
        public final Temp dst;
        public final Temp src;

        public Assign(Temp dst, Temp src) {
            this.dst = dst;
            this.src = src;
        }

        @Override
        public void accept(Visitor v) {
            v.visitAssign(this);
        }

        @Override
        public String toString() {
            return String.format("%s = %s", dst, src);
        }
    }

    public static class LoadVTbl extends Instr {
        public final Temp dst;
        public final VTable vtbl;

        public LoadVTbl(Temp dst, VTable vtbl) {
            this.dst = dst;
            this.vtbl = vtbl;
        }

        @Override
        public void accept(Visitor v) {
            v.visitLoadVTbl(this);
        }

        @Override
        public String toString() {
            return String.format("%s = %s", dst, vtbl.name);
        }
    }

    public static class LoadImm4 extends Instr {
        public final Temp dst;
        public final int value;

        public LoadImm4(Temp dst, int value) {
            this.dst = dst;
            this.value = value;
        }

        @Override
        public void accept(Visitor v) {
            v.visitLoadImm4(this);
        }

        @Override
        public String toString() {
            return String.format("%s = %s", dst, value);
        }
    }

    public static class LoadStrConst extends Instr {
        public final Temp dst;
        public final String value;

        public LoadStrConst(Temp dst, String value) {
            this.dst = dst;
            this.value = value;
        }

        @Override
        public void accept(Visitor v) {
            v.visitLoadStrConst(this);
        }

        @Override
        public String toString() {
            return String.format("%s = %s", dst, quote(value));
        }
    }

    public static class Unary extends Instr {
        public enum Op {
            NEG, LNOT
        }

        public final Op kind;
        public final Temp dst;
        public final Temp operand;

        public Unary(Op kind, Temp dst, Temp operand) {
            this.kind = kind;
            this.dst = dst;
            this.operand = operand;
        }

        @Override
        public void accept(Visitor v) {
            v.visitUnary(this);
        }

        @Override
        public String toString() {
            var opStr = switch (kind) {
                case NEG -> "-";
                case LNOT -> "!";
            };
            return String.format("%s = %s %s", dst, opStr, operand);
        }
    }

    public static class Binary extends Instr {
        public enum Op {
            ADD, SUB, MUL, DIV, MOD, EQU, NEQ, LES, LEQ, GTR, GEQ, LAND, LOR
        }

        public final Op kind;
        public final Temp dst;
        public final Temp lhs;
        public final Temp rhs;

        public Binary(Op kind, Temp dst, Temp lhs, Temp rhs) {
            this.kind = kind;
            this.dst = dst;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public void accept(Visitor v) {
            v.visitBinary(this);
        }

        @Override
        public String toString() {
            var opStr = switch (kind) {
                case ADD -> "+";
                case SUB -> "-";
                case MUL -> "*";
                case DIV -> "/";
                case MOD -> "%";
                case EQU -> "==";
                case NEQ -> "!=";
                case LES -> "<";
                case LEQ -> "<=";
                case GTR -> ">";
                case GEQ -> ">=";
                case LAND -> "&&";
                case LOR -> "||";
            };
            return String.format("%s = (%s %s %s)", dst, lhs, opStr, rhs);
        }
    }

    public static class Branch extends Instr {
        public final Label target;

        public Branch(Label target) {
            this.target = target;
        }

        @Override
        public void accept(Visitor v) {
            v.visitBranch(this);
        }

        @Override
        public String toString() {
            return "branch " + target;
        }
    }

    public static class ConditionalBranch extends Instr {
        public enum Op {
            BEQZ, BNEZ
        }

        public final Op kind;
        public final Temp cond;
        public final Label target;

        public ConditionalBranch(Op kind, Temp cond, Label target) {
            this.kind = kind;
            this.cond = cond;
            this.target = target;
        }

        @Override
        public void accept(Visitor v) {
            v.visitConditionalBranch(this);
        }

        @Override
        public String toString() {
            var opStr = switch (kind) {
                case BEQZ -> "== 0";
                case BNEZ -> "!= 0";
            };
            return String.format("if (%s %s) branch %s", cond, opStr, target);
        }
    }

    public static class Return extends Instr {
        public final Optional<Temp> value;

        public Return(Temp value) {
            this.value = Optional.of(value);
        }

        public Return() {
            this.value = Optional.empty();
        }

        @Override
        public Boolean isReturn() {
            return true;
        }

        @Override
        public void accept(Visitor v) {
            v.visitReturn(this);
        }

        @Override
        public String toString() {
            return "return " + value.map(Objects::toString).orElse("<empty>");
        }
    }

    public static class Parm extends Instr {
        public final Temp value;

        Parm(Temp value) {
            this.value = value;
        }

        @Override
        public void accept(Visitor v) {
            v.visitParm(this);
        }

        @Override
        public String toString() {
            return "parm " + value;
        }
    }

    public static class IndirectCall extends Instr {
        public final Temp dst;
        public final Temp entry;

        public IndirectCall(Temp dst, Temp entry) {
            this.dst = dst;
            this.entry = entry;
        }

        @Override
        public void accept(Visitor v) {
            v.visitIndirectCall(this);
        }

        @Override
        public String toString() {
            return String.format("%s = call %s", dst, entry);
        }
    }

    public static class DirectCall extends Instr {
        public final Temp dst;
        public final Label entry;

        public DirectCall(Temp dst, Label entry) {
            this.dst = dst;
            this.entry = entry;
        }

        @Override
        public void accept(Visitor v) {
            v.visitDirectCall(this);
        }

        @Override
        public String toString() {
            return String.format("%s = call %s", dst, entry);
        }
    }

    public static class Memory extends Instr {
        public enum Op {
            LOAD, STORE
        }

        public final Op kind;
        public final Temp dst;
        public final Temp base;
        public final int offset;

        public Memory(Op kind, Temp dst, Temp base, int offset) {
            this.kind = kind;
            this.dst = dst;
            this.base = base;
            this.offset = offset;
        }

        @Override
        public void accept(Visitor v) {
            v.visitMemory(this);
        }

        @Override
        public String toString() {
            var opStr = offset >= 0 ? "+" : "-";
            return switch (kind) {
                case LOAD -> String.format("%s = *(%s %s %d)", dst, base, opStr, offset);
                case STORE -> String.format("*(%s %s %d) = %s", base, opStr, offset, dst);
            };
        }
    }

    public static class Mark extends Instr {
        public final Label lbl;

        public Mark(Label lbl) {
            this.lbl = lbl;
        }

        @Override
        public Boolean isPseudo() {
            return true;
        }

        @Override
        public Boolean isMark() {
            return true;
        }

        @Override
        public void accept(Visitor v) {
            v.visitMark(this);
        }

        @Override
        public String toString() {
            return lbl + ":";
        }
    }

    public static class Memo extends Instr {
        final String msg;

        public Memo(String msg) {
            this.msg = msg;
        }

        @Override
        public Boolean isPseudo() {
            return true;
        }

        @Override
        public void accept(Visitor v) {
            v.visitMemo(this);
        }

        @Override
        public String toString() {
            return String.format("memo '%s'", msg);
        }
    }

    public interface Visitor {
        default public void visitAssign(Assign instr) {
        }

        default public void visitLoadVTbl(LoadVTbl instr) {
        }

        default public void visitLoadImm4(LoadImm4 instr) {
        }

        default public void visitLoadStrConst(LoadStrConst instr) {
        }

        default public void visitUnary(Unary instr) {
        }

        default public void visitBinary(Binary instr) {
        }

        default public void visitBranch(Branch instr) {
        }

        default public void visitConditionalBranch(ConditionalBranch instr) {
        }

        default public void visitReturn(Return instr) {
        }

        default public void visitParm(Parm instr) {
        }

        default public void visitIndirectCall(IndirectCall instr) {
        }

        default public void visitDirectCall(DirectCall instr) {
        }

        default public void visitMemory(Memory instr) {
        }

        default public void visitMark(Mark instr) {
        }

        default public void visitMemo(Memo instr) {
        }
    }
}
