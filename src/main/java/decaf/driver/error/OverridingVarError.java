package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：overriding variable is not allowed for var 'kittyboy'<br>
 * PA2
 */
public class OverridingVarError extends DecafError {

    private String name;

    public OverridingVarError(Pos pos, String name) {
        super(pos);
        this.name = name;
    }

    @Override
    protected String getErrMsg() {
        return "overriding variable is not allowed for var '" + name + "'";
    }

}
