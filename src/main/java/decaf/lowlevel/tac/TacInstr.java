package decaf.lowlevel.tac;

import decaf.lowlevel.StringUtils;
import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.instr.Temp;
import decaf.lowlevel.label.Label;

import java.util.Optional;

public abstract class TacInstr extends PseudoInstr {
    /**
     * Similar to {@link PseudoInstr#PseudoInstr(Kind, Temp[], Temp[], Label)}
     */
    public TacInstr(Kind kind, Temp[] dsts, Temp[] srcs, Label label) {
        super(kind, dsts, srcs, label);
    }

    /**
     * Similar to {@link PseudoInstr#PseudoInstr(Temp[], Temp[])}
     */
    public TacInstr(Temp[] dsts, Temp[] srcs) {
        super(Kind.SEQ, dsts, srcs, null);
    }

    /**
     * Similar to {@link PseudoInstr#PseudoInstr(Label)}
     */
    public TacInstr(Label label) {
        super(label);
    }

    /**
     * Accept a visitor.
     *
     * @param v visitor
     */
    public abstract void accept(Visitor v);

    /**
     * Visitors of Tac instructions.
     */
    public interface Visitor {
        default void visitAssign(Assign instr) {
            visitOthers(instr);
        }

        default void visitLoadVTbl(LoadVTbl instr) {
            visitOthers(instr);
        }

        default void visitLoadImm4(LoadImm4 instr) {
            visitOthers(instr);
        }

        default void visitLoadStrConst(LoadStrConst instr) {
            visitOthers(instr);
        }

        default void visitUnary(Unary instr) {
            visitOthers(instr);
        }

        default void visitBinary(Binary instr) {
            visitOthers(instr);
        }

        default void visitBranch(Branch instr) {
            visitOthers(instr);
        }

        default void visitCondBranch(CondBranch instr) {
            visitOthers(instr);
        }

        default void visitReturn(Return instr) {
            visitOthers(instr);
        }

        default void visitParm(Parm instr) {
            visitOthers(instr);
        }

        default void visitIndirectCall(IndirectCall instr) {
            visitOthers(instr);
        }

        default void visitDirectCall(DirectCall instr) {
            visitOthers(instr);
        }

        default void visitMemory(Memory instr) {
            visitOthers(instr);
        }

        default void visitMemo(Memo instr) {
            visitOthers(instr);
        }

        default void visitMark(Mark instr) {
            visitOthers(instr);
        }

        default void visitOthers(TacInstr instr) {
        }
    }

    /**
     * Assignment.
     * <pre>
     *     dst = src
     * </pre>
     */
    public static class Assign extends TacInstr {
        public final Temp dst;
        public final Temp src;

        public Assign(Temp dst, Temp src) {
            super(new Temp[]{dst}, new Temp[]{src});
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

    /**
     * Load a virtual table.
     * <pre>
     *     dst = VTABLE&lt;vtbl&gt;
     * </pre>
     */
    public static class LoadVTbl extends TacInstr {
        public final Temp dst;
        public final VTable vtbl;

        public LoadVTbl(Temp dst, VTable vtbl) {
            super(new Temp[]{dst}, new Temp[]{});
            this.dst = dst;
            this.vtbl = vtbl;
        }

        @Override
        public void accept(Visitor v) {
            v.visitLoadVTbl(this);
        }

        @Override
        public String toString() {
            return String.format("%s = %s", dst, vtbl.label.prettyString());
        }
    }

    /**
     * Load a 32-bit signed integer.
     * <pre>
     *     dst = value
     * </pre>
     */
    public static class LoadImm4 extends TacInstr {
        public final Temp dst;
        public final int value;

        public LoadImm4(Temp dst, int value) {
            super(new Temp[]{dst}, new Temp[]{});
            this.dst = dst;
            this.value = value;
        }

        @Override
        public void accept(Visitor v) {
            v.visitLoadImm4(this);
        }

        @Override
        public String toString() {
            return String.format("%s = %d", dst, value);
        }
    }

    /**
     * Load constant string.
     * <pre>
     *     dst = value
     * </pre>
     */
    public static class LoadStrConst extends TacInstr {
        public final Temp dst;
        public final String value;

        public LoadStrConst(Temp dst, String value) {
            super(new Temp[]{dst}, new Temp[]{});
            this.dst = dst;
            this.value = value;
        }

        @Override
        public void accept(Visitor v) {
            v.visitLoadStrConst(this);
        }

        @Override
        public String toString() {
            return String.format("%s = %s", dst, StringUtils.quote(value));
        }
    }

    /**
     * Unary instruction.
     * <pre>
     *     dst = op operand
     * </pre>
     */
    public static class Unary extends TacInstr {
        public final Op op;
        public final Temp dst;
        public final Temp operand;

        public enum Op {
            NEG, LNOT
        }

        public Unary(Op op, Temp dst, Temp operand) {
            super(new Temp[]{dst}, new Temp[]{operand});
            this.op = op;
            this.dst = dst;
            this.operand = operand;
        }

        @Override
        public void accept(Visitor v) {
            v.visitUnary(this);
        }

        @Override
        public String toString() {
            var opStr = switch (op) {
                case NEG -> "-";
                case LNOT -> "!";
            };
            return String.format("%s = %s %s", dst, opStr, operand);
        }
    }

    /**
     * Binary instruction.
     * <pre>
     *     dst = (lhs op rhs)
     * </pre>
     */
    public static class Binary extends TacInstr {
        public final Op op;
        public final Temp dst;
        public final Temp lhs;
        public final Temp rhs;

        public enum Op {
            ADD, SUB, MUL, DIV, MOD, EQU, NEQ, LES, LEQ, GTR, GEQ, LAND, LOR
        }

        public Binary(Op op, Temp dst, Temp lhs, Temp rhs) {
            super(new Temp[]{dst}, new Temp[]{lhs, rhs});
            this.op = op;
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
            var opStr = switch (op) {
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

    /**
     * Branch instruction.
     * <pre>
     *     branch target
     * </pre>
     */
    public static class Branch extends TacInstr {
        public final Label target;

        public Branch(Label target) {
            super(Kind.JMP, new Temp[]{}, new Temp[]{}, target);
            this.target = target;
        }

        @Override
        public void accept(Visitor v) {
            v.visitBranch(this);
        }

        @Override
        public String toString() {
            return String.format("branch %s", target.prettyString());
        }
    }

    /**
     * Branch instruction.
     * <pre>
     *     if (cond == 0) branch target
     *     if (cond != 0) branch target
     * </pre>
     */
    public static class CondBranch extends TacInstr {
        public final Op op;
        public final Temp cond;
        public final Label target;

        public enum Op {
            BEQZ, BNEZ
        }

        public CondBranch(Op op, Temp cond, Label target) {
            super(Kind.COND_JMP, new Temp[]{}, new Temp[]{cond}, target);
            this.op = op;
            this.cond = cond;
            this.target = target;
        }

        @Override
        public void accept(Visitor v) {
            v.visitCondBranch(this);
        }

        @Override
        public String toString() {
            var opStr = switch (op) {
                case BEQZ -> "== 0";
                case BNEZ -> "!= 0";
            };
            return String.format("if (%s %s) branch %s", cond, opStr, target.prettyString());
        }
    }

    /**
     * Return instruction.
     * <pre>
     *     return value?
     * </pre>
     */
    public static class Return extends TacInstr {
        public final Optional<Temp> value;

        public Return(Temp value) {
            super(Kind.RET, new Temp[]{}, new Temp[]{value}, null);
            this.value = Optional.of(value);
        }

        public Return() {
            super(Kind.RET, new Temp[]{}, new Temp[]{}, null);
            this.value = Optional.empty();
        }

        @Override
        public void accept(Visitor v) {
            v.visitReturn(this);
        }

        @Override
        public String toString() {
            var sb = new StringBuilder();
            sb.append("return");
            value.ifPresent(v -> sb.append(" ").append(v));
            return sb.toString();
        }
    }

    /**
     * Push a parameter.
     * <pre>
     *     parm value
     * </pre>
     */
    public static class Parm extends TacInstr {
        public final Temp value;

        public Parm(Temp value) {
            super(new Temp[]{}, new Temp[]{value});
            this.value = value;
        }

        @Override
        public void accept(Visitor v) {
            v.visitParm(this);
        }

        @Override
        public String toString() {
            return String.format("parm %s", value);
        }
    }

    /**
     * Call by address (which is stored in a temp).
     * <pre>
     *     {dst =}? call entry
     * </pre>
     */
    public static class IndirectCall extends TacInstr {
        public final Optional<Temp> dst;
        public final Temp entry;

        public IndirectCall(Temp dst, Temp entry) {
            super(new Temp[]{dst}, new Temp[]{entry});
            this.dst = Optional.of(dst);
            this.entry = entry;
        }

        public IndirectCall(Temp entry) {
            super(new Temp[]{}, new Temp[]{entry});
            this.dst = Optional.empty();
            this.entry = entry;
        }

        @Override
        public void accept(Visitor v) {
            v.visitIndirectCall(this);
        }

        @Override
        public String toString() {
            var sb = new StringBuffer();
            dst.ifPresent(d -> sb.append(d).append(" = "));
            sb.append("call ").append(entry);
            return sb.toString();
        }
    }

    /**
     * Call by label.
     * <pre>
     *     {dst =}? call entry
     * </pre>
     */
    public static class DirectCall extends TacInstr {
        public final Optional<Temp> dst;
        public final Label entry;

        public DirectCall(Temp dst, Label entry) {
            super(new Temp[]{dst}, new Temp[]{});
            this.dst = Optional.of(dst);
            this.entry = entry;
        }

        public DirectCall(Label entry) {
            super(new Temp[]{}, new Temp[]{});
            this.dst = Optional.empty();
            this.entry = entry;
        }

        public DirectCall(Temp dst, Intrinsic intrinsic) {
            super(new Temp[]{dst}, new Temp[]{});
            this.dst = Optional.of(dst);
            this.entry = intrinsic.entry;
        }

        public DirectCall(Intrinsic intrinsic) {
            super(new Temp[]{}, new Temp[]{});
            this.dst = Optional.empty();
            this.entry = intrinsic.entry;
        }

        @Override
        public void accept(Visitor v) {
            v.visitDirectCall(this);
        }

        @Override
        public String toString() {
            var sb = new StringBuffer();
            dst.ifPresent(d -> sb.append(d).append(" = "));
            sb.append("call ").append(entry.prettyString());
            return sb.toString();
        }
    }

    /**
     * Memory access: load/store.
     * <pre>
     *     dst = *(base + offset)
     *     *(base + offset) = dst
     * </pre>
     */
    public static class Memory extends TacInstr {
        public final Op op;
        public final Temp dst;
        public final Temp base;
        public final int offset;

        public enum Op {
            LOAD, STORE
        }

        public Memory(Op op, Temp dst, Temp base, int offset) {
            super(op.equals(Op.LOAD) ? new Temp[]{dst} : new Temp[]{},
                    op.equals(Op.LOAD) ? new Temp[]{base} : new Temp[]{dst, base});
            this.op = op;
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
            var sign = offset >= 0 ? "+" : "-";
            var value = offset >= 0 ? offset : -offset;
            return switch (op) {
                case LOAD -> String.format("%s = *(%s %s %d)", dst, base, sign, value);
                case STORE -> String.format("*(%s %s %d) = %s", base, sign, offset, dst);
            };
        }
    }

    /**
     * Comment.
     * <pre>
     *     memo 'msg'
     * </pre>
     */
    public static class Memo extends TacInstr {
        final String msg;

        public Memo(String msg) {
            super(new Temp[]{}, new Temp[]{});
            this.msg = msg;
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

    /**
     * Label.
     * <pre>
     * label:
     * </pre>
     */
    public static class Mark extends TacInstr {
        public Mark(Label label) {
            super(label);
        }

        @Override
        public void accept(Visitor v) {
            v.visitMark(this);
        }

        @Override
        public String toString() {
            return String.format("%s:", label.prettyString());
        }
    }
}
