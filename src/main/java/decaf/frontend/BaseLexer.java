package decaf.frontend;

import decaf.Driver;
import decaf.error.DecafError;
import decaf.error.IntTooLargeError;
import decaf.tree.Pos;

import java.io.IOException;

public abstract class BaseLexer {

    private Parser parser;

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    abstract int yylex() throws IOException;

    abstract Pos getLocation();

    protected void issueError(DecafError error) {
        Driver.getDriver().issueError(error);
    }

    protected void setSemantic(Pos where, SemValue v) {
        v.loc = where;
        parser.yylval = v;
    }

    protected int keyword(int code) {
        setSemantic(getLocation(), SemValue.createKeyword(code));
        return code;
    }

    protected int operator(int code) {
        setSemantic(getLocation(), SemValue.createOperator(code));
        return code;
    }

    protected int boolConst(boolean bval) {
        setSemantic(getLocation(), SemValue.createBoolLit(bval));
        return Parser.BOOL_LIT;
    }

    protected int StringConst(String sval, Pos loc) {
        setSemantic(loc, SemValue.createStringLit(sval));
        return Parser.STRING_LIT;
    }

    protected int intConst(String ival) {
        try {
            setSemantic(getLocation(), SemValue.createIntLit(Integer.decode(ival)));
        } catch (NumberFormatException e) {
            Driver.getDriver().issueError(new IntTooLargeError(getLocation(), ival));
        }
        return Parser.INT_LIT;
    }

    protected int identifier(String name) {
        setSemantic(getLocation(), SemValue.createIdentifier(name));
        return Parser.IDENTIFIER;
    }

    public void diagnose() throws IOException {
        while (yylex() != 0) {
            System.out.println(parser.yylval);
        }
    }
}
