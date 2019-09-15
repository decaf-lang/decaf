package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：array base type must be non-void type<br>
 * PA2
 */
public class BadArrElementError extends DecafError {

    public BadArrElementError(Pos pos) {
        super(pos);
    }

    @Override
    protected String getErrMsg() {
        return "array element type must be non-void known type";
    }

}
