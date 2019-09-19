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

    @Override
    public String prettyString() {
        return String.format("FUNCTION<%s.%s>", clazz, method);
    }

    @Override
    public boolean isFunc() {
        return true;
    }

    private FuncLabel() {
        super(Kind.FUNC, "main");
        this.clazz = "Main";
        this.method = "main";
    }

    /**
     * Special function label: main entry.
     */
    public static FuncLabel MAIN_LABEL = new FuncLabel() {
        @Override
        public String prettyString() {
            return name;
        }
    };
}
