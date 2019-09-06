package decaf.error;

import decaf.tree.Pos;

/**
 * Lexer error.
 *
 * Decaf only support the following escape characters: \n, \t, \r, \", and \\. Others like \a are illegal.
 *
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
