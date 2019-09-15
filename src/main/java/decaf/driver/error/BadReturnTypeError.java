package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：incompatible return: int[] given, int expected<br>
 * PA2
 */
public class BadReturnTypeError extends DecafError {

    private String expect;

    private String given;

    public BadReturnTypeError(Pos pos, String expect, String given) {
        super(pos);
        this.expect = expect;
        this.given = given;
    }

    @Override
    protected String getErrMsg() {
        return "incompatible return: " + given + " given, " + expect
                + " expected";
    }
}
