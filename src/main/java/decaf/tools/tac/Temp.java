package decaf.tools.tac;

public class Temp {
    public final int index;

    Temp(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "_T" + index;
    }
}
