package decaf.tools.tac;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static decaf.utils.MiscUtils.quote;

public abstract class Tac {

    public static class Prog {
        public final List<VTable> vtables;

        public final List<Func> funcs;

        public Prog(List<VTable> vtables, List<Func> funcs) {
            this.vtables = vtables;
            this.funcs = funcs;
        }

        public void printTo(PrintWriter pw) {
            for (var vtbl : vtables) {
                vtbl.printTo(pw);
            }
            for (var func : funcs) {
                func.printTo(pw);
            }
        }
    }


    /**
     * Virtual table. In TAC, a named virtual table consists the following items (in order):
     * - the pointer to the virtual table of its parent class (if any), or null (if none)
     * - the class name (a string literal)
     * - labels of all member methods (static methods are EXCLUDED), which include those inherited from
     * super classes. For these inherited/overriden items, the offsets in this virtual table MUST be consistent with
     * those ones (virtual table of the parent class), for example:
     * {{{
     * VTABLE(_Animal) {
     * <empty>
     * Animal
     * _Animal.GetMom;       <-- offset 8 (byte)
     * _Animal.GetHeight;    <-- offset 12
     * _Animal.InitAnimal;   <-- offset 16
     * }
     * <p>
     * VTABLE(_Cow) {
     * _Animal
     * Cow
     * _Animal.GetMom;       <-- inherited from _Animal, offset 8
     * _Cow.GetHeight;       <-- override _Animal's GetHeight, offset 12
     * _Animal.InitAnimal;   <-- inherited from _Animal, offset 16
     * _Cow.InitCow;         <-- newly defined
     * _Cow.IsSpottedCow;    <-- newly defined
     * }
     * }}}
     * Note that each item takes 4 bytes, and the offsets 8, 12, and 16 are consistent.
     */
    public static class VTable {
        /**
         * Name. NOTE: may differs from `className`.
         */
        public final String name;

        /**
         * The name of the class.
         */
        public final String className;

        /**
         * Virtual table of its parent class (if any).
         */
        public final Optional<VTable> parent;

        public int getSize() {
            return 8 + 4 * memberMethods.size();
        }

        public List<Label> getItems() {
            return memberMethods;
        }


        /**
         * Labels of all member methods.
         */
        List<Label> memberMethods = new ArrayList<>();

        List<String> memberVariables = new ArrayList<>();

        VTable(String className, Optional<VTable> parent) {
            this.name = ".V<" + className + ">";
            this.className = className;
            this.parent = parent;
        }

        int getObjectSize() {
            return 4 + 4 * memberVariables.size();
        }

        public void printTo(PrintWriter pw) {
            pw.println("VTABLE(" + name + ") {");
            if (parent.isPresent()) {
                pw.println("    " + parent.get().name);
            } else {
                pw.println("    <empty>");
            }
            pw.println("    " + className);
            for (var l : memberMethods) {
                pw.println("    " + l.name + ";");
            }
            pw.println("}");
            pw.println();
        }
    }


    /**
     * Function. In TAC, a function consists of:
     * - a label of the entry point, so that our call instruction can jump into it and execute from the first instruction
     * - a sequence of instructions to be executed
     */
    public static class Func {
        public final Label entry;

        public List<Instr> getInstrSeq() {
            return instrSeq;
        }

        public int getUsedTempCount() {
            return tempUsed;
        }

        List<Instr> instrSeq = new ArrayList<>();

        int tempUsed;

        Func(Label entry) {
            this.entry = entry;
        }

        void add(Instr instr) {
            instrSeq.add(instr);
        }

        public boolean isIntrinsic() {
            return false;
        }

        public void printTo(PrintWriter pw) {
            pw.println("FUNCTION(" + entry.name + ") {");
            for (var instr : instrSeq) {
                if (instr.isMark()) {
                    pw.println(instr);
                } else {
                    pw.println("    " + instr);
                }
            }
            pw.println("}");
            pw.println();
        }
    }

    public static abstract class Instr {
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

        public abstract void accept(InstrVisitor v);

        public abstract String toString();
    }

    public static class Assign extends Instr {
        public final Temp dst;
        public final Temp src;

        public Assign(Temp dst, Temp src) {
            this.dst = dst;
            this.src = src;
        }

        @Override
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
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
        public void accept(InstrVisitor v) {
            v.visitMemo(this);
        }

        @Override
        public String toString() {
            return String.format("memo '%s'", msg);
        }
    }

    public interface InstrVisitor {
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


    public static class Temp {
        public final int index;

        Temp(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return ".T" + index;
        }
    }


    /**
     * Label.
     * Specific program locations and all procedures/functions have labels.
     */
    public static class Label {
        public final String name;

        public final boolean target;

        Label(String name, boolean target) {
            this.name = name;
            this.target = target;
        }

        Label(String name) {
            this.name = name;
            this.target = false;
        }

        public static Label MAIN_LABEL = new Label(".main");

        @Override
        public String toString() {
            return name;
        }
    }
}
