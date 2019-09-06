package decaf.tools.tac;

/**
 * Label.
 * Specific program locations and all procedures/functions have labels.
 */
public class Label {
    public final String name;

    public final boolean target;

    Label(String name, boolean target) {
        this.name = name;
        this.target = target;
    }

    Label(String name) {
        this.name = name;
        this.target = false;
    }

    public static Label MAIN_LABEL = new Label("main");

    @Override
    public String toString() {
        return name;
    }
}
