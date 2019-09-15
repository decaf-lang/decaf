package decaf.lowlevel;

public class Temp implements Comparable<Temp> {
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