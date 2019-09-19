package decaf.lowlevel.label;

/**
 * Labels for virtual tables.
 */
public class VTableLabel extends Label {
    /**
     * Class name.
     */
    public final String clazz;

    public VTableLabel(String clazz) {
        super(Kind.VTABLE, String.format("_V_%s", clazz));
        this.clazz = clazz;
    }

    @Override
    public boolean isVTable() {
        return true;
    }

    @Override
    public String prettyString() {
        return String.format("VTABLE<%s>", clazz);
    }
}
