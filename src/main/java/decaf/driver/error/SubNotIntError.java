package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：array subscript must be an integer<br>
 * PA2
 */
public class SubNotIntError extends DecafError {

    public SubNotIntError(Pos pos) {
        super(pos);
    }

    @Override
    protected String getErrMsg() {
        return "array subscript must be an integer";
    }

}
