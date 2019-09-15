package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * can not use this in static function
 * PA2
 */
public class ThisInStaticFuncError extends DecafError {

    public ThisInStaticFuncError(Pos pos) {
        super(pos);
    }

    @Override
    protected String getErrMsg() {
        return "can not use this in static function";
    }

}
