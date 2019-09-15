package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：class 'zig' not found<br>
 * PA2
 */
public class ClassNotFoundError extends DecafError {

    private String name;

    public ClassNotFoundError(Pos pos, String name) {
        super(pos);
        this.name = name;
    }

    @Override
    protected String getErrMsg() {
        return "class '" + name + "' not found";
    }

}
