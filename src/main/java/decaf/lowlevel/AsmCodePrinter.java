package decaf.lowlevel;

import decaf.lowlevel.instr.NativeInstr;
import decaf.lowlevel.label.Label;

/**
 * Assembly code pretty printer.
 */
public class AsmCodePrinter {
    private StringBuilder sb = new StringBuilder();

    protected final String INDENTS = "    ";

    protected final String END_LINE = "\n";

    protected final String COMMENT_PROMPT = "#";

    /**
     * Format print, with indents.
     *
     * @param fmt  format
     * @param args arguments
     */
    public void print(String fmt, Object... args) {
        sb.append(INDENTS);
        sb.append(String.format(fmt, args));
    }

    /**
     * Format print a line, with indents.
     *
     * @param fmt  format
     * @param args arguments
     */
    public void println(String fmt, Object... args) {
        sb.append(INDENTS);
        sb.append(String.format(fmt, args));
        sb.append(END_LINE);
    }

    /**
     * Simply print a newline.
     */
    public void println() {
        sb.append(END_LINE);
    }

    /**
     * Print a label.
     *
     * @param label label
     */
    public void printLabel(Label label) {
        sb.append(label.name);
        sb.append(":");
        sb.append(END_LINE);
    }

    /**
     * Print a label with comment.
     *
     * @param label   label
     * @param comment comment
     */
    public void printLabel(Label label, String comment) {
        sb.append(label.name);
        sb.append(":");
        sb.append("  ").append(COMMENT_PROMPT).append(' ').append(comment).append(END_LINE);
    }

    /**
     * Print an instruction.
     *
     * @param instr instruction
     */
    public void printInstr(NativeInstr instr) {
        if (instr.isLabel()) {
            sb.append(instr.label);
            sb.append(":");
        } else {
            sb.append(INDENTS);
            sb.append(instr);
        }

        sb.append(END_LINE);
    }

    /**
     * Print an instruction, with comment.
     *
     * @param instr   instruction
     * @param comment comment
     */
    public void printInstr(NativeInstr instr, String comment) {
        if (instr.isLabel()) {
            sb.append(instr.label);
            sb.append(":");
        } else {
            sb.append(INDENTS);
            sb.append(instr);
        }

        sb.append("  ").append(COMMENT_PROMPT).append(' ').append(comment).append(END_LINE);
    }

    /**
     * Print a comment.
     *
     * @param comment comment
     */
    public void printComment(String comment) {
        sb.append(INDENTS);
        sb.append(COMMENT_PROMPT).append(' ');
        sb.append(comment);
        sb.append(END_LINE);
    }

    /**
     * Finish printing. Get the plain text of the assembly code.
     *
     * @return assembly code
     */
    public String close() {
        return sb.toString();
    }
}
