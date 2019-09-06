package decaf.tools.tac;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Function. In TAC, a function consists of:
 * - a label of the entry point, so that our call instruction can jump into it and execute from the first instruction
 * - a sequence of instructions to be executed
 */
public class Function {
    public final Label entry;

    public List<Instr> getInstrSeq() {
        return instrSeq;
    }

    public int getUsedTempCount() {
        return tempUsed;
    }

    List<Instr> instrSeq = new ArrayList<>();

    int tempUsed;

    Function(Label entry) {
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
