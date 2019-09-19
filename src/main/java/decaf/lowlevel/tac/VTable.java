package decaf.lowlevel.tac;

import decaf.lowlevel.label.FuncLabel;
import decaf.lowlevel.label.VTableLabel;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A virtual table, consists the following items (in order, in memory, the offset is counted in bytes):
 * <ol>
 *     <li>Reference to the virtual table of its super class (if any) (offset 0).</li>
 *     <li>Class name (offset 4, which is indeed a pointer to the name string).</li>
 *     <li>Labels of all member methods (static methods are EXCLUDED, start from offset 8, each 4 bytes), which include
 *     those inherited from super classes. For those inherited/overriden items, the offsets in virtual table MUST be
 *     the SAME with the ones in super classes' tables.</li>
 * </ol>
 */
public class VTable {
    /**
     * Label.
     */
    public final VTableLabel label;

    /**
     * The name of the class.
     */
    public final String className;

    /**
     * Virtual table of its super class (if any).
     */
    public final Optional<VTable> parent;

    public int getSize() {
        return 8 + 4 * memberMethods.size();
    }

    public List<FuncLabel> getItems() {
        return memberMethods;
    }

    /**
     * Labels of all member methods.
     */
    List<FuncLabel> memberMethods = new ArrayList<>();

    List<String> memberVariables = new ArrayList<>();

    VTable(String className, Optional<VTable> parent) {
        this.label = new VTableLabel(className);
        this.className = className;
        this.parent = parent;
    }

    int getObjectSize() {
        return 4 + 4 * memberVariables.size();
    }

    public void printTo(PrintWriter pw) {
        pw.println(label.prettyString() + ":");
        if (parent.isPresent()) {
            pw.println("    " + parent.get().label.prettyString());
        } else {
            pw.println("    NULL");
        }
        pw.println("    \"" + className + "\"");
        for (var l : memberMethods) {
            pw.println("    " + l.prettyString());
        }
        pw.println();
    }
}
