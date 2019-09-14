package decaf.backend.asm;

import decaf.instr.Label;
import decaf.instr.NativeInstr;

public class AsmCodePrinter {
    private StringBuilder sb = new StringBuilder();

    protected final String INDENTS = "    ";

    protected final String END_LINE = "\n";

    public void print(String fmt, Object... args) {
        sb.append(INDENTS);
        sb.append(String.format(fmt, args));
    }

    public void println(String fmt, Object... args) {
        sb.append(INDENTS);
        sb.append(String.format(fmt, args));
        sb.append(END_LINE);
    }

    public void println() {
        sb.append(END_LINE);
    }

    public void printLabel(Label lbl) {
        sb.append(lbl.name);
        sb.append(":");
        sb.append(END_LINE);
    }

    public void printLabel(Label lbl, String comment) {
        sb.append(lbl.name);
        sb.append(":");
        sb.append("    # ");
        sb.append(comment);
        sb.append(END_LINE);
    }

    public void printInstr(NativeInstr instr) {
        sb.append(INDENTS);
        sb.append(instr);
        sb.append(END_LINE);
    }

    public void printInstr(NativeInstr instr, String comment) {
        sb.append(INDENTS);
        sb.append(instr);
        sb.append("    # ");
        sb.append(comment);
        sb.append(END_LINE);
    }

    public void printComment(String comment) {
        sb.append(INDENTS);
        sb.append("# ");
        sb.append(comment);
        sb.append(END_LINE);
    }

    public String close() {
        return sb.toString();
    }
}
