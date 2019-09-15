package decaf.instr;

import decaf.instr.tac.Intrinsic;
import decaf.instr.tac.TAC;

import java.util.Optional;

public abstract class TacInstr extends InstrLike {

    public TacInstr(Kind kind, Temp[] dsts, Temp[] srcs, Label jumpTo, Object... imms) {
        super(kind, "", dsts, srcs, jumpTo, imms);
    }

    public TacInstr(Temp[] dsts, Temp[] srcs, Object... imms) {
        super(Kind.SEQ, "", dsts, srcs, null, imms);
    }

    public TacInstr(Label label) {
        super(label);
    }

    @Override
    public String toString() {
        return String.format(getFormat(), getArgs());
    }

    public boolean isReturn() {
        return false;
    }

    public abstract void accept(InstrVisitor v);

    public interface InstrVisitor {
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

    public static class Assign extends TacInstr {
        public final Temp dst;
        public final Temp src;

        public Assign(Temp dst, Temp src) {
            super(new Temp[]{dst}, new Temp[]{src});
            this.dst = dst;
            this.src = src;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitAssign(this);
        }

        @Override
        public String getFormat() {
            return "%s = %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{dst, src};
        }
    }

    public static class LoadVTbl extends TacInstr {
        public final Temp dst;
        public final TAC.VTable vtbl;

        public LoadVTbl(Temp dst, TAC.VTable vtbl) {
            super(new Temp[]{dst}, new Temp[]{}, vtbl);
            this.dst = dst;
            this.vtbl = vtbl;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitLoadVTbl(this);
        }

        @Override
        public String getFormat() {
            return "%s = %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{dst, vtbl};
        }
    }

    public static class LoadImm4 extends TacInstr {
        public final Temp dst;
        public final int value;

        public LoadImm4(Temp dst, int value) {
            super(new Temp[]{dst}, new Temp[]{}, value);
            this.dst = dst;
            this.value = value;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitLoadImm4(this);
        }

        @Override
        public String getFormat() {
            return "%s = %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{dst, value};
        }
    }

    public static class LoadStrConst extends TacInstr {
        public final Temp dst;
        public final String value;

        public LoadStrConst(Temp dst, String value) {
            super(new Temp[]{dst}, new Temp[]{}, value);
            this.dst = dst;
            this.value = value;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitLoadStrConst(this);
        }

        @Override
        public String getFormat() {
            return "%s = %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{dst, value};
        }
    }

    public static class Unary extends TacInstr {
        public final Op kind;
        public final Temp dst;
        public final Temp operand;

        public Unary(Op kind, Temp dst, Temp operand) {
            super(new Temp[]{dst}, new Temp[]{operand});
            this.kind = kind;
            this.dst = dst;
            this.operand = operand;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitUnary(this);
        }

        @Override
        public String getFormat() {
            return "%s = %s %s";
        }

        @Override
        public Object[] getArgs() {
            var opStr = switch (kind) {
                case NEG -> "-";
                case LNOT -> "!";
            };
            return new Object[]{dst, opStr, operand};
        }

        public enum Op {
            NEG, LNOT
        }
    }

    public static class Binary extends TacInstr {
        public final Op kind;
        public final Temp dst;
        public final Temp lhs;
        public final Temp rhs;

        public Binary(Op kind, Temp dst, Temp lhs, Temp rhs) {
            super(new Temp[]{dst}, new Temp[]{lhs, rhs});
            this.kind = kind;
            this.dst = dst;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitBinary(this);
        }

        @Override
        public String getFormat() {
            return "%s = (%s %s %s)";
        }

        @Override
        public Object[] getArgs() {
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
            return new Object[]{dst, lhs, opStr, rhs};
        }

        public enum Op {
            ADD, SUB, MUL, DIV, MOD, EQU, NEQ, LES, LEQ, GTR, GEQ, LAND, LOR
        }
    }

    public static class Branch extends TacInstr {
        public final Label target;

        public Branch(Label target) {
            super(Kind.JMP, new Temp[]{}, new Temp[]{}, target);
            this.target = target;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitBranch(this);
        }

        @Override
        public String getFormat() {
            return "branch %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{target};
        }
    }

    public static class CondBranch extends TacInstr {
        public final Op kind;
        public final Temp cond;
        public final Label target;

        public CondBranch(Op kind, Temp cond, Label target) {
            super(Kind.COND_JMP, new Temp[]{}, new Temp[]{cond}, target);
            this.kind = kind;
            this.cond = cond;
            this.target = target;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitCondBranch(this);
        }

        @Override
        public String getFormat() {
            return "if (%s %s) branch %s";
        }

        @Override
        public Object[] getArgs() {
            var opStr = switch (kind) {
                case BEQZ -> "== 0";
                case BNEZ -> "!= 0";
            };
            return new Object[]{cond, opStr, target};
        }

        public enum Op {
            BEQZ, BNEZ
        }
    }

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
        public boolean isReturn() {
            return true;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitReturn(this);
        }

        @Override
        public String getFormat() {
            return "return %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{value.map(Temp::toString).orElse("<empty>")};
        }
    }

    public static class Parm extends TacInstr {
        public final Temp value;

        public Parm(Temp value) {
            super(new Temp[]{}, new Temp[]{value});
            this.value = value;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitParm(this);
        }

        @Override
        public String getFormat() {
            return "parm %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{value};
        }
    }

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
        public void accept(InstrVisitor v) {
            v.visitIndirectCall(this);
        }

        @Override
        public String getFormat() {
            return "%scall %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{dst.map(Temp::toString).orElse(""), entry};
        }
    }

    public static class DirectCall extends TacInstr {
        public final Optional<Temp> dst;
        public final Label entry;

        public DirectCall(Temp dst, Label entry) {
            super(new Temp[]{dst}, new Temp[]{}, entry);
            this.dst = Optional.of(dst);
            this.entry = entry;
        }

        public DirectCall(Label entry) {
            super(new Temp[]{}, new Temp[]{}, entry);
            this.dst = Optional.empty();
            this.entry = entry;
        }

        public DirectCall(Temp dst, Intrinsic intrinsic) {
            super(new Temp[]{dst}, new Temp[]{}, intrinsic.entry);
            this.dst = Optional.of(dst);
            this.entry = intrinsic.entry;
            this.intrinsic = intrinsic;
        }

        public DirectCall(Intrinsic intrinsic) {
            super(new Temp[]{}, new Temp[]{}, intrinsic.entry);
            this.dst = Optional.empty();
            this.entry = intrinsic.entry;
            this.intrinsic = intrinsic;
        }

        private Intrinsic intrinsic;

        public boolean isIntrinsicCall() {
            return intrinsic != null;
        }

        public Intrinsic getIntrinsic() {
            return intrinsic;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitDirectCall(this);
        }

        @Override
        public String getFormat() {
            return "%scall %s";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{dst.map(Temp::toString).orElse(""), entry};
        }
    }

    public static class Memory extends TacInstr {
        public final Op kind;
        public final Temp dst;
        public final Temp base;
        public final int offset;

        public Memory(Op kind, Temp dst, Temp base, int offset) {
            super(kind.equals(Op.LOAD) ? new Temp[]{dst} : new Temp[]{},
                    kind.equals(Op.LOAD) ? new Temp[]{base} : new Temp[]{dst, base});
            this.kind = kind;
            this.dst = dst;
            this.base = base;
            this.offset = offset;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitMemory(this);
        }

        @Override
        public String getFormat() {
            return switch (kind) {
                case LOAD -> "%s = *(%s %s %d)";
                case STORE -> "*(%s %s %d) = %s";
            };
        }

        @Override
        public Object[] getArgs() {
            var opStr = offset >= 0 ? "+" : "-";
            return switch (kind) {
                case LOAD -> new Object[]{dst, base, opStr, offset};
                case STORE -> new Object[]{base, opStr, offset, dst};
            };
        }

        public enum Op {
            LOAD, STORE
        }
    }

    public static class Memo extends TacInstr {
        final String msg;

        public Memo(String msg) {
            super(new Temp[]{}, new Temp[]{}, msg);
            this.msg = msg;
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitMemo(this);
        }

        @Override
        public String getFormat() {
            return "memo '%s'";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{msg};
        }
    }

    public static class Mark extends TacInstr {
        public final Label label;

        public Mark(Label label) {
            super(label);
            this.label = label;
        }

        @Override
        public String getFormat() {
            return "%s:";
        }

        @Override
        public Object[] getArgs() {
            return new Object[]{label};
        }

        @Override
        public void accept(InstrVisitor v) {
            v.visitMark(this);
        }
    }
}
