package decaf.lowlevel.label;

/**
 * Labels for functions.
 */
public class FuncLabel extends Label {
    /**
     * Class name.
     */
    public final String clazz;

    /**
     * Method name.
     */
    public final String method;

    public FuncLabel(String clazz, String method) {
        super(Kind.FUNC, String.format("_L_%s_%s", clazz, method));
        this.clazz = clazz;
        this.method = method;
    }

    private FuncLabel() {
        super(Kind.FUNC, "main");
        this.clazz = "Main";
        this.method = "main";
    }

    public static FuncLabel MAIN_LABEL = new FuncLabel();

    public String prettyString() {
        return clazz + "." + method;
    }

    @Override
    public boolean isFunc() {
        return true;
    }
}
