package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：illegal class inheritance (should be acyclic)<br>
 * PA2
 */
public class BadInheritanceError extends DecafError {

    public BadInheritanceError(Pos pos) {
        super(pos);
    }

    @Override
    protected String getErrMsg() {
        return "illegal class inheritance (should be acyclic)";
    }

}
