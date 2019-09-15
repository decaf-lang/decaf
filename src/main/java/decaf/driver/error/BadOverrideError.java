package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：overriding method 'tooold' doesn't match the type signature in class
 * 'duckyaya'<br>
 * PA2
 */
public class BadOverrideError extends DecafError {

    private String funcName;

    private String parentName;

    public BadOverrideError(Pos pos, String funcName,
                            String parentName) {
        super(pos);
        this.funcName = funcName;
        this.parentName = parentName;
    }

    @Override
    protected String getErrMsg() {
        return "overriding method '" + funcName
                + "' doesn't match the type signature in class '" + parentName
                + "'";
    }

}
