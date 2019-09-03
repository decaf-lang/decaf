package decaf.parsing;

import decaf.tree.Pos;
import decaf.tree.Tree;

import java.util.List;

/**
 * A semantic value simply simulates a "union" (which we see from C++, etc.), so that we can store all kinds of
 * tree nodes in one class.
 * <p>
 * This class should be visible only in this package.
 */
class SemValue {

    public int code;

    public Pos loc;

    public int typeTag;

    public int intValue;

    public boolean boolValue;

    public String stringValue;

    public String ident;

    public Tree.Id id;

    public List<Tree.ClassDef> clist;

    /**
     * field list
     */
    public List<Tree.Field> flist;

    public List<Tree.VarDef> vlist;

    public List<Tree.LocalVarDef> lvlist;


    /**
     * statement list
     */
    public List<Tree.Stmt> slist;

    public List<Tree.Expr> elist;

    public Tree.TopLevel prog;

    public Tree.ClassDef cdef;

    public Tree.VarDef vdef;
    public Tree.LocalVarDef lvdef;

    public Tree.MethodDef fdef;

    public Tree.TypeLit type;

    public Tree.Stmt stmt;

    public Tree.Expr expr;

    public Tree.LValue lvalue;

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
        v.intValue = value;
        return v;
    }

    static SemValue createBoolLit(boolean value) {
        SemValue v = new SemValue();
        v.code = Tokens.BOOL_LIT;
        v.boolValue = value;
        return v;
    }

    static SemValue createStringLit(String value) {
        SemValue v = new SemValue();
        v.code = Tokens.STRING_LIT;
        v.stringValue = value;
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
        v.ident = name;
        return v;
    }

    /**
     * 获取这个语义值的字符串表示<br>
     *
     * 我们建议你在构造词法分析器之前先阅读一下这个函数。
     *
     * TODO rewrite or deprecate this!
     *
     public String toString() {
     String msg;
     switch (code) {
     // 关键字
     case Parser.BOOL:
     msg = "keyword  : bool";
     break;
     case Parser.BREAK:
     msg = "keyword  : break";
     break;
     case Parser.CLASS:
     msg = "keyword  : class";
     break;
     case Parser.ELSE:
     msg = "keyword  : else";
     break;
     case Parser.EXTENDS:
     msg = "keyword  : extends";
     break;
     case Parser.FOR:
     msg = "keyword  : for";
     break;
     case Parser.IF:
     msg = "keyword  : if";
     break;
     case Parser.INT:
     msg = "keyword  : int";
     break;
     case Parser.INSTANCEOF:
     msg = "keyword  : instanceof";
     break;
     case Parser.NEW:
     msg = "keyword  : new";
     break;
     case Parser.NULL:
     msg = "keyword  : null";
     break;
     case Parser.PRINT:
     msg = "keyword  : Print";
     break;
     case Parser.READ_INTEGER:
     msg = "keyword  : ReadInteger";
     break;
     case Parser.READ_LINE:
     msg = "keyword  : ReadLine";
     break;
     case Parser.RETURN:
     msg = "keyword  : return";
     break;
     case Parser.STRING:
     msg = "keyword  : string";
     break;
     case Parser.THIS:
     msg = "keyword  : this";
     break;
     case Parser.VOID:
     msg = "keyword  : void";
     break;
     case Parser.WHILE:
     msg = "keyword  : while";
     break;
     case Parser.STATIC:
     msg = "keyword : static";
     break;

     // 常量
     case Parser.LITERAL:
     switch (typeTag) {
     case Tree.INT:
     case Tree.BOOL:
     msg = "constant : " + literal;
     break;
     default:
     msg = "constant : " + MiscUtils.quote((String)literal);
     }
     break;

     // 标识符
     case Parser.IDENTIFIER:
     msg = "identifier: " + ident;
     break;

     // 操作符
     case Parser.AND:
     msg = "operator : &&";
     break;
     case Parser.EQUAL:
     msg = "operator : ==";
     break;
     case Parser.GREATER_EQUAL:
     msg = "operator : >=";
     break;
     case Parser.LESS_EQUAL:
     msg = "operator : <=";
     break;
     case Parser.NOT_EQUAL:
     msg = "operator : !=";
     break;
     case Parser.OR:
     msg = "operator : ||";
     break;
     default:
     msg = "operator : " + (char) code;
     break;
     }
     return (String.format("%-15s%s", loc, msg));
     }*/

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
