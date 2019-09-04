package decaf.parsing;

import decaf.tree.Pos;
import decaf.tree.Tree;
import decaf.utils.MiscUtils;

import java.util.List;

/**
 * A semantic value simply simulates a "union" (which we see from C++, etc.), so that we can store all kinds of
 * tree nodes in one class.
 * <p>
 * This class should be visible only in this package.
 */
class SemValue {

    // Position
    Pos pos;

    // For lexer
    int code;
    int intLit;
    boolean boolLit;
    String stringLit;
    String identifier;

    // For parser
    Tree.ClassDef clazz;
    List<Tree.ClassDef> classList;

    Tree.Field field;
    List<Tree.Field> fieldList;

    Tree.Var var; // member var or local var
    List<Tree.LocalVarDef> varList; // a list can only contain local vars

    Tree.TypeLit type;

    Tree.Stmt stmt;
    List<Tree.Stmt> stmtList;
    Tree.Block block;

    Tree.Expr expr;
    List<Tree.Expr> exprList;
    Tree.LValue lValue;

    Tree.Id id;

    static SemValue createKeyword(int code) {
        SemValue v = new SemValue();
        v.code = code;
        return v;
    }

    static SemValue createOperator(int code) {
        SemValue v = new SemValue();
        v.code = code;
        return v;
    }

    static SemValue createIntLit(int value) {
        SemValue v = new SemValue();
        v.code = Tokens.INT_LIT;
        v.intLit = value;
        return v;
    }

    static SemValue createBoolLit(boolean value) {
        SemValue v = new SemValue();
        v.code = Tokens.BOOL_LIT;
        v.boolLit = value;
        return v;
    }

    static SemValue createStringLit(String value) {
        SemValue v = new SemValue();
        v.code = Tokens.STRING_LIT;
        v.stringLit = value;
        return v;
    }

    /**
     * 创建一个标识符的语义值
     *
     * @param name 标识符的名字
     * @return 对应的语义值（标识符名字存放在sval域）
     */
    static SemValue createIdentifier(String name) {
        SemValue v = new SemValue();
        v.code = Tokens.IDENTIFIER;
        v.identifier = name;
        return v;
    }

    /**
     * For debug
     */
    public String toString() {
        String msg = switch (code) {
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
            case Tokens.INT_LIT -> "int literal : " + intLit;
            case Tokens.BOOL_LIT -> "bool literal : " + boolLit;
            case Tokens.STRING_LIT -> "string literal : " + MiscUtils.quote(stringLit);
            case Tokens.IDENTIFIER -> "identifier: " + identifier;
            case Tokens.AND -> "operator : &&";
            case Tokens.EQUAL -> "operator : ==";
            case Tokens.GREATER_EQUAL -> "operator : >=";
            case Tokens.LESS_EQUAL -> "operator : <=";
            case Tokens.NOT_EQUAL -> "operator : !=";
            case Tokens.OR -> "operator : ||";
            default -> "operator : " + (char) code;
        };
        return (String.format("%-15s%s", pos, msg));
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
