package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：function 'gotoMars' expects 1 argument(s) but 3 given<br>
 * PA2
 */
public class BadArgCountError extends DecafError {

    private String method;

    private int expect;

    private int count;

    public BadArgCountError(Pos pos, String method, int expect,
                            int count) {
        super(pos);
        this.method = method;
        this.expect = expect;
        this.count = count;
    }

    @Override
    protected String getErrMsg() {
        return "function '" + method + "' expects " + expect
                + " argument(s) but " + count + " given";
    }
}
