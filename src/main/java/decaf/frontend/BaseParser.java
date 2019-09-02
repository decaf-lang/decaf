package decaf.frontend;

import decaf.Driver;
import decaf.tree.Tree;
import decaf.error.DecafError;
import decaf.error.MsgError;
import decaf.frontend.Lexer;

public abstract class BaseParser {
    private Lexer lexer;

    protected Tree.TopLevel tree;

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public Tree.TopLevel getTree() {
        return tree;
    }

    protected void issueError(DecafError error) {
        Driver.getDriver().issueError(error);
    }

    void yyerror(String msg) {
        Driver.getDriver().issueError(
                new MsgError(lexer.getLocation(), msg));
    }

    SemValue yylval;

    int token = -1;

    int yylex() {
        token = -1;
        try {
            token = lexer.yylex();
        } catch (Exception e) {
            yyerror("lexer error: " + e.getMessage());
        }

        return token;
    }

    abstract boolean parse();

    public Tree.TopLevel parseFile() {
        parse();
        return tree;
    }

    /**
     * 获得操作符的字符串表示
     *
     * @param op 操作符的符号码
     * @return 该操作符的字符串形式
     */
    public static String opStr(Tree.UnaryOp op) {
        return switch (op) {
            case NEG -> "-";
            case NOT -> "!";
        };
    }

    // TODO: rewrite using case expression
    public static String opStr(Tree.BinaryOp opCode) {
        switch (opCode) {
            case AND:
                return "&&";
            case EQ:
                return "==";
            case GE:
                return ">=";
            case LE:
                return "<=";
            case NE:
                return "!=";
            case OR:
                return "||";
            case ADD:
                return "+";
            case SUB:
                return "-";
            case MUL:
                return "*";
            case DIV:
                return "/";
            case MOD:
                return "%";
            case GT:
                return ">";
            case LT:
                return "<";
            default:
                return "<unknown>";
        }
    }
}
