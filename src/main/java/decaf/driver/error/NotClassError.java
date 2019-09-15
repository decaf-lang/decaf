package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * string is not a class type.
 */
public class NotClassError extends DecafError {

    private String type;

    public NotClassError(String type, Pos pos) {
        super(pos);
        this.type = type;
    }

    @Override
    protected String getErrMsg() {
        return type + " is not a class type";
    }

}
