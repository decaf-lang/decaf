package decaf.lowlevel.tac;

import java.io.PrintWriter;
import java.util.List;

/**
 * A TAC program, consists of many virtual tables and functions.
 */
public class TacProg {
    /**
     * Virtual tables.
     */
    public final List<VTable> vtables;

    /**
     * Functions.
     */
    public final List<TacFunc> funcs;

    public TacProg(List<VTable> vtables, List<TacFunc> funcs) {
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
