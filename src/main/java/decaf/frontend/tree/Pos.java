package decaf.frontend.tree;

/**
 * Position of a token/tree node in the source file.
 */
public class Pos implements Comparable<Pos> {

    /**
     * Undefined position.
     */
    public static final Pos NoPos = new Pos(-1, -1);

    /**
     * Line number, start from 1.
     */
    public final int line;

    /**
     * Column number, also start from 1.
     */
    public final int column;

    /**
     * Create a position.
     *
     * @param line   its line number
     * @param column its column number
     */
    public Pos(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return "(" + line + "," + column + ")";
    }

    /**
     * Positions are compared by lexicographic order, i.e. first compare line numbers, and if they are same,
     * compare their column numbers.
     *
     * @param that another position
     * @return comparing result
     */
    @Override
    public int compareTo(Pos that) {
        if (line > that.line) {
            return 1;
        }
        if (line < that.line) {
            return -1;
        }
        if (column > that.column) {
            return 1;
        }
        if (column < that.column) {
            return -1;
        }
        return 0;
    }
}
