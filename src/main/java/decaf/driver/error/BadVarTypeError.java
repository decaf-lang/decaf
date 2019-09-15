package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：cannot declare identifier 'boost' as void type<br>
 * PA2
 */
public class BadVarTypeError extends DecafError {

    private String name;

    public BadVarTypeError(Pos pos, String name) {
        super(pos);
        this.name = name;
    }

    @Override
    protected String getErrMsg() {
        return "cannot declare identifier '" + name + "' as void type";
    }

}
