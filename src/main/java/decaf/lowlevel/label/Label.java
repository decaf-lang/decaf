package decaf.lowlevel.label;

/**
 * High-level abstraction of labels.
 * In the low level, a label is just an "address" of the "instruction memory".
 *
 * @see VTableLabel
 * @see FuncLabel
 * @see IntrinsicLabel
 */
public class Label implements Comparable<Label> {
    /**
     * Label kind.
     * <ul>
     *     <li>{@code TEMP}: temporary, typically a temporary jump target</li>
     *     <li>{@code VTABLE}: for virtual tables</li>
     *     <li>{@code FUNC}: for function entries</li>
     *     <li>{@code INTRINSIC}: reserved for intrinsic functions, see {@link decaf.lowlevel.tac.Intrinsic}</li>
     * </ul>
     */
    public enum Kind {
        TEMP, VTABLE, FUNC, INTRINSIC
    }

    public final Kind kind;

    /**
     * Name, should be the bare representation.
     */
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

    /**
     * Bare representation of this label, may differ from {@link #prettyString}.
     *
     * @return bare representation
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Pretty representation of this label.
     *
     * @return pretty representation
     */
    public String prettyString() {
        return name;
    }

    @Override
    public int compareTo(Label that) {
        return this.name.compareTo(that.name);
    }
}
