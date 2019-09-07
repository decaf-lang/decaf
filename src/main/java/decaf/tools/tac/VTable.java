package decaf.tools.tac;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
public class VTable {
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
