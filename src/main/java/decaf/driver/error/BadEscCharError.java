package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * Lexer error.
 * <p>
 * Decaf only support the following escape characters: \n, \t, \r, \", and \\. Others like \a are illegal.
 * <p>
 * Example:
 * <pre>
 *   illegal escape character
 *
 *   str = "\a";
 *          ^
 * </pre>
 */
public class BadEscCharError extends DecafError {

    public BadEscCharError(Pos pos) {
        super(pos);
    }

    @Override
    protected String getErrMsg() {
        return "illegal escape character";
    }
}
