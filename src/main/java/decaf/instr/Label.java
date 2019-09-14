package decaf.instr;

public class Label implements Comparable<Label> {
    public final String name;

    public Label(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Label that) {
        return this.name.compareTo(that.name);
    }
}
