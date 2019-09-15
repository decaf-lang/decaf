package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：incompatible argument 3: int given, bool expected<br>
 * 3表示发生错误的是第三个参数<br>
 * PA2
 */
public class BadArgTypeError extends DecafError {

    private int count;

    private String given;

    private String expect;

    public BadArgTypeError(Pos pos, int count, String given,
                           String expect) {
        super(pos);
        this.count = count;
        this.given = given;
        this.expect = expect;
    }

    @Override
    protected String getErrMsg() {
        return "incompatible argument " + count + ": " + given + " given, "
                + expect + " expected";
    }

}
