package decaf.parsing;

import decaf.error.DecafError;
import decaf.error.ErrorIssuer;
import decaf.error.IntTooLargeError;
import decaf.tree.Pos;

import java.io.IOException;

/**
 * The abstract lexer specifies all methods that a concrete lexer should implement, and provide a couple of helper
 * methods.
 */
public abstract class AbstractLexer {

    /**
     * Get position of the current token.
     */
    abstract Pos getPos();

    /**
     * Get the parsed token.
     *
     * @throws IOException
     */
    abstract int yylex() throws IOException;

    private AbstractParser parser;
    private ErrorIssuer issuer;

    /**
     * When lexing, we need to set parser's semantic value.
     */
    void setup(AbstractParser parser, ErrorIssuer issuer) {
        this.parser = parser;
        this.issuer = issuer;
    }

    /**
     * Helper method used by the concrete lexer: record a keyword.
     *
     * @param code the token's code
     * @return just `code`
     */
    protected int keyword(int code) {
        parser.semValue = new SemValue(code, getPos());
        return code;
    }

    /**
     * Helper method used by the concrete lexer: record an operator (with a single character).
     *
     * @param code the token's code
     * @return just `code`
     */
    protected int operator(int code) {
        parser.semValue = new SemValue(code, getPos());
        return code;
    }

    /**
     * Helper method used by the concrete lexer: record a constant integer.
     *
     * @param value the text representation of the integer
     * @return a token INT_LIT
     */
    protected int intConst(String value) {
        parser.semValue = new SemValue(Tokens.INT_LIT, getPos());
        try {
            parser.semValue.intVal = Integer.decode(value);
        } catch (NumberFormatException e) {
            issueError(new IntTooLargeError(getPos(), value));
        }
        return Tokens.INT_LIT;
    }

    /**
     * Helper method used by the concrete lexer: record a constant bool.
     *
     * @param value the text representation of the bool
     * @return a token BOOL_LIT
     */
    protected int boolConst(boolean value) {
        parser.semValue = new SemValue(Tokens.BOOL_LIT, getPos());
        parser.semValue.boolVal = value;
        return Tokens.BOOL_LIT;
    }

    /**
     * Helper method used by the concrete lexer: record a constant string.
     *
     * @param value the unquoted string
     * @return a token STRING_LIT
     */
    protected int StringConst(String value, Pos pos) {
        parser.semValue = new SemValue(Tokens.STRING_LIT, pos);
        parser.semValue.strVal = value;
        return Tokens.STRING_LIT;
    }

    /**
     * Helper method used by the concrete lexer: record an identifier.
     *
     * @param name the identifier name
     * @return a token IDENTIFIER
     */
    protected int identifier(String name) {
        parser.semValue = new SemValue(Tokens.IDENTIFIER, getPos());
        parser.semValue.strVal = name;
        return Tokens.IDENTIFIER;
    }

    protected void issueError(DecafError error) {
        issuer.issue(error);
    }

    /**
     * For debug: print out all tokens.
     *
     * @throws IOException
     */
    public void diagnose() throws IOException {
        while (yylex() != 0) {
            System.out.println(parser.semValue);
        }
    }
}
