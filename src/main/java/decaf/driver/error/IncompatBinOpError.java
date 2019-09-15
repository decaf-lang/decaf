package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：incompatible operands: int + bool<br>
 * PA2
 */
public class IncompatBinOpError extends DecafError {

    private String left;

    private String right;

    private String op;

    public IncompatBinOpError(Pos pos, String left, String op,
                              String right) {
        super(pos);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    protected String getErrMsg() {
        return "incompatible operands: " + left + " " + op + " " + right;
    }

}
