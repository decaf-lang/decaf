package decaf.instr;

public class Temp implements Comparable<Temp> {
    public final int index;

    public Temp(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return ".T" + index;
    }

    @Override
    public int compareTo(Temp that) {
        return this.index - that.index;
    }
}