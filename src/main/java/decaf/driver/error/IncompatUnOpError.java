package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：incompatible operand: - int[]<br>
 * PA2
 */
public class IncompatUnOpError extends DecafError {

    private String op;

    private String expr;

    public IncompatUnOpError(Pos pos, String op, String type) {
        super(pos);
        this.op = op;
        this.expr = type;
    }

    @Override
    protected String getErrMsg() {
        return "incompatible operand: " + op + " " + expr;
    }

}
