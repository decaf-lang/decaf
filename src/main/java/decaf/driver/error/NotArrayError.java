package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：[] can only be applied to arrays<br>
 * PA2
 */
public class NotArrayError extends DecafError {

    public NotArrayError(Pos pos) {
        super(pos);
    }

    @Override
    protected String getErrMsg() {
        return "[] can only be applied to arrays";
    }

}
