package decaf.printing;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * 用于缩进输出的类<br>
 * 使用时注意不要自行输出"\r\n"或"\n"进行换行，而要使用该类的println系列函数进行换行<br>
 */
public class IndentPrinter extends PrintWriter {
    private int step;

    private StringBuilder indent;

    private boolean newLineBegin;

    /**
     * @param out
     * @param step 每次缩进变化的空格数
     */
    public IndentPrinter(OutputStream out, int step) {
        this(new OutputStreamWriter(out), step);
    }

    /**
     * @param out
     */
    public IndentPrinter(OutputStream out) {
        this(new OutputStreamWriter(out), 4);
    }

    /**
     * @param out
     * @param step 每次缩进变化的空格数
     */
    public IndentPrinter(Writer out, int step) {
        super(out);
        this.step = step;
        indent = new StringBuilder();
        newLineBegin = true;
    }

    /**
     * 增加缩进
     */
    public void incIndent() {
        for (int i = 0; i < step; i++) {
            indent.append(" ");
        }
    }

    /**
     * 减少缩进
     */
    public void decIndent() {
        indent.setLength(indent.length() - step);
    }

    @Override
    public void println() {
        super.println();
        newLineBegin = true;
    }

    private void writeIndent() {
        if (newLineBegin) {
            newLineBegin = false;
            print(indent);
        }
    }

    @Override
    public void write(char[] buf, int off, int len) {
        writeIndent();
        super.write(buf, off, len);
    }

    @Override
    public void write(int c) {
        writeIndent();
        super.write(c);
    }

    @Override
    public void write(String s, int off, int len) {
        writeIndent();
        super.write(s, off, len);
    }

}
