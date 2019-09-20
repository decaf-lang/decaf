package decaf.lowlevel.log;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A printer which handles indentation, and multiple outputs.
 */
public final class IndentPrinter {
    /**
     * Constructor.
     *
     * @param outs      output streams
     * @param autoFlush if true, the {@code println}, or {@code formatLn} methods will automatically flush the output
     *                  once they are called
     * @param indents   indentation spaces (default 4)
     */
    public IndentPrinter(List<OutputStream> outs, boolean autoFlush, int indents) {
        this.indents = indents;

        writers = new ArrayList<>();
        for (var out : outs) {
            writers.add(new PrintWriter(out, autoFlush));
        }
    }

    /**
     * @see #IndentPrinter(List, boolean, int)
     */
    public IndentPrinter(List<OutputStream> outs, boolean autoFlush) {
        this(outs, autoFlush, 4);
    }

    /**
     * @see #IndentPrinter(List, boolean, int)
     */
    public IndentPrinter(List<OutputStream> outs) {
        this(outs, false, 4);
    }

    /**
     * Constructor.
     *
     * @param out       output stream
     * @param autoFlush if true, the {@code println} or {@code formatLn} methods will automatically flush the output
     *                  once they are called
     * @param indents   indentation spaces (default 4)
     */
    public IndentPrinter(OutputStream out, boolean autoFlush, int indents) {
        this(Arrays.asList(out), autoFlush, indents);
    }

    /**
     * @see #IndentPrinter(OutputStream, boolean, int)
     */
    public IndentPrinter(OutputStream out, boolean autoFlush) {
        this(out, autoFlush, 4);
    }

    /**
     * @see #IndentPrinter(OutputStream, boolean, int)
     */
    public IndentPrinter(OutputStream out) {
        this(out, false, 4);
    }

    /**
     * Increase indentation (by one level).
     */
    public void incIndent() {
        for (int i = 0; i < indents; i++) {
            spaces.append(' ');
        }
    }

    /**
     * Decrease indentation (by one level).
     */
    public void decIndent() {
        spaces.setLength(spaces.length() - indents);
    }

    /**
     * Print a string.
     */
    public void print(String s) {
        write(s, false);
    }

    /**
     * Print a string, with end of line.
     */
    public void println(String s) {
        write(s, true);
    }

    /**
     * Print end of line.
     */
    public void println() {
        write("", true);
    }

    /**
     * Print an object, will call its {@link #toString} method.
     */
    public void print(Object o) {
        write(o.toString(), false);
    }

    /**
     * Print an object with end of line, will call its {@link #toString} method.
     */
    public void println(Object o) {
        write(o.toString(), true);
    }

    /**
     * Format print.
     *
     * @param fmt  format
     * @param args arguments
     */
    public void format(String fmt, Object... args) {
        write(String.format(fmt, args), false);
    }

    /**
     * Format print with end of line.
     *
     * @param fmt  format
     * @param args arguments
     */
    public void formatLn(String fmt, Object... args) {
        write(String.format(fmt, args), true);
    }

    /**
     * Pretty format print. Saying "pretty", we mean every collection will be nicely printed, such as
     * {@code [1, 2, 3]}.
     *
     * @param fmt  format
     * @param args arguments
     */
    public void prettyFormat(String fmt, Object... args) {
        for (var i = 0; i < args.length; i++) {
            if (args[i] instanceof Collection) {
                args[i] = format((Collection<?>) args[i]);
            }
        }

        write(String.format(fmt, args), false);
    }

    /**
     * Pretty format print with end of line. See {@link #prettyFormat(String, Object...)}.
     *
     * @param fmt  format
     * @param args arguments
     */
    public void prettyFormatLn(String fmt, Object... args) {
        for (var i = 0; i < args.length; i++) {
            if (args[i] instanceof Collection) {
                args[i] = format((Collection<?>) args[i]);
            }
        }

        write(String.format(fmt, args), true);
    }

    /**
     * Flush outputs force any buffered output to be written out.
     */
    public void flush() {
        for (var writer : writers) {
            writer.flush();
        }
    }

    private void write(String s, boolean endsLine) {
        for (var writer : writers) {
            if (isNewLine) {
                writer.print(spaces.toString());
            }

            if (endsLine) {
                writer.println(s);
            } else {
                writer.print(s);
            }
        }

        isNewLine = endsLine;
    }

    private String format(Collection<?> c) {
        String s = c.stream().map(Object::toString).collect(Collectors.joining(", "));
        return String.format("[%s]", s);
    }

    private List<PrintWriter> writers;

    private int indents;

    private StringBuilder spaces = new StringBuilder();

    private boolean isNewLine = true;
}
