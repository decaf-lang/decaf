package decaf.tools.tac;

import java.io.PrintWriter;
import java.util.List;

public class TacProgram {
    public final List<VTable> vtables;

    public final List<Function> functions;

    public TacProgram(List<VTable> vtables, List<Function> functions) {
        this.vtables = vtables;
        this.functions = functions;
    }

    public void printTo(PrintWriter pw) {
        for (var vtbl : vtables) {
            vtbl.printTo(pw);
        }
        for (var func : functions) {
            func.printTo(pw);
        }
    }
}
