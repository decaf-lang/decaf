package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：'break' is only allowed inside a loop<br>
 * PA2
 */
public class BreakOutOfLoopError extends DecafError {

    public BreakOutOfLoopError(Pos pos) {
        super(pos);
    }

    @Override
    protected String getErrMsg() {
        return "'break' is only allowed inside a loop";
    }

}
