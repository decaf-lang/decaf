package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：unrecognized char: '@'<br>
 * PA1
 */
public class UnrecogCharError extends DecafError {

    private char c;

    public UnrecogCharError(Pos pos, char c) {
        super(pos);
        this.c = c;
    }

    @Override
    protected String getErrMsg() {
        return "unrecognized character '" + c + "'";
    }
}
