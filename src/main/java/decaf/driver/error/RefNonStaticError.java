package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * can not reference a non-static field 'kylin' from static method from 'dove'
 * PA2
 */
public class RefNonStaticError extends DecafError {

    private String from;

    private String ref;

    public RefNonStaticError(Pos pos, String from, String ref) {
        super(pos);
        this.from = from;
        this.ref = ref;
    }

    @Override
    protected String getErrMsg() {
        return "can not reference a non-static field '" + ref
                + "' from static method '" + from + "'";
    }

}
