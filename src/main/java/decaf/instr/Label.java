package decaf.instr;

public class Label implements Comparable<Label> {
    public enum Kind {
        TEMP, VTABLE, FUNC, INTRINSIC
    }

    public final Kind kind;

    public final String name;

    public Label(Kind kind, String name) {
        this.kind = kind;
        this.name = name;
    }

    public Label(String name) {
        this(Kind.TEMP, name);
    }

    public boolean isVTable() {
        return kind.equals(Kind.VTABLE);
    }

    public boolean isFunc() {
        return kind.equals(Kind.FUNC);
    }

    public boolean isIntrinsic() {
        return kind.equals(Kind.INTRINSIC);
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
