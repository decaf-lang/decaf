package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：illegal newline in string constant "this is stri"<br>
 * PA1
 */
public class NewlineInStrError extends DecafError {

    private String str;

    public NewlineInStrError(Pos pos, String str) {
        super(pos);
        this.str = str;
    }

    @Override
    protected String getErrMsg() {
        return "illegal newline in string constant " + str;
    }

}
