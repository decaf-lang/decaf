package decaf.frontend.parsing;

import decaf.frontend.tree.Pos;
import decaf.frontend.tree.Tree;
import decaf.utils.MiscUtils;

import java.util.List;

/**
 * A semantic value simply simulates a "union" (which we see from C++, etc.), so that we can store all kinds of
 * tree nodes in one class.
 * <p>
 * This class should be visible only in this package.
 */
class SemValue {
    enum Kind {
        TOKEN, CLASS, CLASS_LIST, FIELD, FIELD_LIST, VAR, VAR_LIST, TYPE, STMT, STMT_LIST, BLOCK, EXPR, EXPR_LIST,
        LVALUE, ID
    }

    // Kind
    final Kind kind;

    // Position and possibly needed extra position
    Pos pos;
    Pos pos2;

    // Make sure every semantic value has a position.
    SemValue(Kind kind, Pos pos) {
        this.kind = kind;
        this.pos = pos;
    }

    // For lexer
    int code;
    int intVal;
    boolean boolVal;
    String strVal;

    // Create a lexer semantic value. This is called by the helper methods in AbstractLexer.java.
    SemValue(int code, Pos pos) {
        this.kind = Kind.TOKEN;
        this.pos = pos;
        this.code = code;
    }

    // For parser
    Tree.ClassDef clazz;
    List<Tree.ClassDef> classList;

    Tree.Field field;
    List<Tree.Field> fieldList;

    // a raw var (local/member) is stored using two variables: type, id
    List<Tree.LocalVarDef> varList; // a list can only contain local vars

    Tree.TypeLit type;

    Tree.Stmt stmt;
    List<Tree.Stmt> stmtList;
    Tree.Block block;

    Tree.Expr expr;
    List<Tree.Expr> exprList;
    Tree.LValue lValue;

    Tree.Id id;

    /**
     * Pretty print sem value. For debug.
     */
    @Override
    public String toString() {
        String msg = switch (kind) {
            case TOKEN -> switch (code) {
                case Tokens.BOOL -> "keyword  : bool";
                case Tokens.BREAK -> "keyword  : break";
                case Tokens.CLASS -> "keyword  : class";
                case Tokens.ELSE -> "keyword  : else";
                case Tokens.EXTENDS -> "keyword  : extends";
                case Tokens.FOR -> "keyword  : for";
                case Tokens.IF -> "keyword  : if";
                case Tokens.INT -> "keyword  : int";
                case Tokens.INSTANCE_OF -> "keyword : instanceof";
                case Tokens.NEW -> "keyword  : new";
                case Tokens.NULL -> "keyword  : null";
                case Tokens.PRINT -> "keyword  : Print";
                case Tokens.READ_INTEGER -> "keyword  : ReadInteger";
                case Tokens.READ_LINE -> "keyword  : ReadLine";
                case Tokens.RETURN -> "keyword  : return";
                case Tokens.STRING -> "keyword  : string";
                case Tokens.THIS -> "keyword  : this";
                case Tokens.VOID -> "keyword  : void";
                case Tokens.WHILE -> "keyword  : while";
                case Tokens.STATIC -> "keyword : static";
                case Tokens.INT_LIT -> "int literal : " + intVal;
                case Tokens.BOOL_LIT -> "bool literal : " + boolVal;
                case Tokens.STRING_LIT -> "string literal : " + MiscUtils.quote(strVal);
                case Tokens.IDENTIFIER -> "identifier: " + strVal;
                case Tokens.AND -> "operator : &&";
                case Tokens.EQUAL -> "operator : ==";
                case Tokens.GREATER_EQUAL -> "operator : >=";
                case Tokens.LESS_EQUAL -> "operator : <=";
                case Tokens.NOT_EQUAL -> "operator : !=";
                case Tokens.OR -> "operator : ||";
                default -> "operator : " + (char) code;
            };
            case CLASS -> "CLASS: " + clazz;
            case CLASS_LIST -> "CLASS_LIST: " + classList;
            case FIELD -> "FIELD: " + field;
            case FIELD_LIST -> "FIELD_LIST: " + fieldList;
            case VAR -> "VAR: " + type + " " + id;
            case VAR_LIST -> "VAR_LIST: " + varList;
            case TYPE -> "TYPE: " + type;
            case STMT -> "STMT: " + stmt;
            case STMT_LIST -> "STMT_LIST: " + stmtList;
            case BLOCK -> "BLOCK: " + block;
            case EXPR -> "EXPR: " + expr;
            case EXPR_LIST -> "EXPR_LIST: " + exprList;
            case LVALUE -> "LVALUE: " + lValue;
            case ID -> "ID: " + id;
        };
        return String.format("%-9s%s", pos, msg);
    }

    /**
     * 辅助模版（切勿直接调用）
     *
     * @param $$ 对应 YACC 语义动作中的 $$
     * @param $1 对应 YACC 语义动作中的 $1
     * @param $2 对应 YACC 语义动作中的 $2
     * @param $3 对应 YACC 语义动作中的 $3
     * @param $4 对应 YACC 语义动作中的 $4
     * @param $5 对应 YACC 语义动作中的 $5
     * @param $6 对应 YACC 语义动作中的 $6
     */
    void UserAction(SemValue $$, SemValue $1, SemValue $2, SemValue $3,
                    SemValue $4, SemValue $5, SemValue $6) {
        /*
         * 这个函数作用是提供一个模版给你编写你的 YACC 语义动作。 因为在一般编辑器中编写 YACC 脚本的时候没法充分调用 IDE
         * 的各种编辑功能， 因此专门开辟一个函数。使用的时候你只需要把语义动作写在下面的花括号中， 然后连同花括号一起复制-粘贴到 YACC
         * 脚本对应位置即可。
         */
        {

        }
    }
}
