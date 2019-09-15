package decaf.instr.tac;

import decaf.instr.Label;
import decaf.instr.TacInstr;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class TAC {

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
        public final Label label;

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
            this.label = new Label(Label.Kind.VTABLE, "_V_" + className);
            this.className = className;
            this.parent = parent;
        }

        int getObjectSize() {
            return 4 + 4 * memberVariables.size();
        }

        public void printTo(PrintWriter pw) {
            pw.println("VTABLE(" + label + ") {");
            if (parent.isPresent()) {
                pw.println("    " + parent.get().label);
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
    public static class Func implements Comparable<Func> {
        public final Label entry;

        public final int numArgs;

        Func(Label entry, int numArgs) {
            this.entry = entry;
            this.numArgs = numArgs;
        }

        public List<TacInstr> getInstrSeq() {
            return instrSeq;
        }

        public int getUsedTempCount() {
            return tempUsed;
        }

        List<TacInstr> instrSeq = new ArrayList<>();

        int tempUsed;

        void add(TacInstr instr) {
            instrSeq.add(instr);
        }

        public boolean isIntrinsic() {
            return false;
        }

        public void printTo(PrintWriter pw) {
            pw.println("FUNCTION(" + entry.name + ") {");
            for (var instr : instrSeq) {
                if (instr.isLabel()) {
                    pw.println(instr);
                } else {
                    pw.println("    " + instr);
                }
            }
            pw.println("}");
            pw.println();
        }

        @Override
        public int compareTo(Func that) {
            return this.entry.name.compareTo(that.entry.name);
        }
    }

    public static final Label MAIN_LABEL = new Label(Label.Kind.FUNC, "main");
}
