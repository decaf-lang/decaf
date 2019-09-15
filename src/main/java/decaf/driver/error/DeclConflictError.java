package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：declaration of 'abcde' here conflicts with earlier declaration at (3,2)<br>
 * PA2
 */
public class DeclConflictError extends DecafError {

    private Pos earlier;

    private String name;

    public DeclConflictError(Pos pos, String name, Pos earlier) {
        super(pos);
        this.name = name;
        this.earlier = earlier;
    }

    @Override
    protected String getErrMsg() {
        return "declaration of '" + name
                + "' here conflicts with earlier declaration at " + earlier;
    }

}
