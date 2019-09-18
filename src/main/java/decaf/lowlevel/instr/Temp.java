package decaf.lowlevel.instr;

/**
 * A pseudo register, or a temporary variable.
 * <p>
 * For short, we simply call it temp.
 */
public class Temp implements Comparable<Temp> {
    /**
     * Index, must be unique inside a function.
     */
    public final int index;

    public Temp(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "_T" + index;
    }

    @Override
    public int compareTo(Temp that) {
        return this.index - that.index;
    }
}