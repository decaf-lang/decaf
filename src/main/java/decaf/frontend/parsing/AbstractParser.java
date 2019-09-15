package decaf.frontend.parsing;

import decaf.driver.ErrorIssuer;
import decaf.driver.error.MsgError;
import decaf.frontend.tree.Pos;
import decaf.frontend.tree.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * The abstract parser specifies all methods that a concrete parser should implement, and provide a couple of helper
 * methods.
 */
public abstract class AbstractParser {

    /**
     * The entry to the concrete parser.
     *
     * @return if parse succeeds?
     */
    abstract boolean parse();

    private AbstractLexer lexer;

    private ErrorIssuer issuer;

    /**
     * When parsing, we need to interact with the lexer.
     */
    void setup(AbstractLexer lexer, ErrorIssuer issuer) {
        this.lexer = lexer;
        this.issuer = issuer;
        // The generated parser expects that, right now, `token` records the first token returned by lexer.
        token = nextToken();
    }

    /**
     * Final parsing result to be written by the concrete parser. Remember in `Decaf.jacc`, we designed an action for
     * TopLevel:
     * {{{
     * tree = new Tree.TopLevel($1.clist, $1.loc);
     * }}}
     */
    Tree.TopLevel tree;

    /**
     * Helper variable used by the concrete parser: the semantic value of the current token.
     * <p>
     * Set by the lexer.
     */
    protected SemValue semValue;

    /**
     * Helper variable used by the concrete parser: the current token.
     */
    protected int token;

    /**
     * Helper method used by the concrete parser: fetch the next token.
     *
     * @return the next token.
     */
    protected int nextToken() {
        token = -1;
        try {
            token = lexer.yylex();
        } catch (Exception e) {
            yyerror("lexer error: " + e.getMessage());
        }

        return token;
    }

    protected SemValue svClass(Tree.ClassDef clazz) {
        var v = new SemValue(SemValue.Kind.CLASS, clazz == null ? Pos.NoPos : clazz.pos);
        v.clazz = clazz;
        return v;
    }

    protected SemValue svClasses(Tree.ClassDef... classes) {
        var v = new SemValue(SemValue.Kind.CLASS_LIST, classes.length == 0 ? Pos.NoPos : classes[0].pos);
        v.classList = new ArrayList<>();
        v.classList.addAll(Arrays.asList(classes));
        return v;
    }

    protected SemValue svField(Tree.Field field) {
        var v = new SemValue(SemValue.Kind.FIELD, field == null ? Pos.NoPos : field.pos);
        v.field = field;
        return v;
    }

    protected SemValue svFields(Tree.Field... fields) {
        var v = new SemValue(SemValue.Kind.FIELD_LIST, fields.length == 0 ? Pos.NoPos : fields[0].pos);
        v.fieldList = new ArrayList<>();
        v.fieldList.addAll(Arrays.asList(fields));
        return v;
    }

    protected SemValue svVar(Tree.TypeLit type, Tree.Id id, Pos pos) {
        var v = new SemValue(SemValue.Kind.VAR, pos);
        v.type = type;
        v.id = id;
        return v;
    }

    protected SemValue svVarLocal(Tree.TypeLit type, Tree.Id id, Pos assignPos, Tree.Expr initVal, Pos pos) {
        var v = new SemValue(SemValue.Kind.VAR, pos);
        v.type = type;
        v.id = id;
        v.pos2 = assignPos;
        v.expr = initVal;
        return v;
    }

    protected SemValue svVars(Tree.LocalVarDef... vars) {
        var v = new SemValue(SemValue.Kind.FIELD_LIST, vars.length == 0 ? Pos.NoPos : vars[0].pos);
        v.varList = new ArrayList<>();
        v.varList.addAll(Arrays.asList(vars));
        return v;
    }

    protected SemValue svType(Tree.TypeLit type) {
        var v = new SemValue(SemValue.Kind.TYPE, type == null ? Pos.NoPos : type.pos);
        v.type = type;
        return v;
    }

    protected SemValue svStmt(Tree.Stmt stmt) {
        var v = new SemValue(SemValue.Kind.STMT, stmt == null ? Pos.NoPos : stmt.pos);
        v.stmt = stmt;
        return v;
    }

    protected SemValue svStmts(Tree.Stmt... stmts) {
        var v = new SemValue(SemValue.Kind.STMT_LIST, stmts.length == 0 ? Pos.NoPos : stmts[0].pos);
        v.stmtList = new ArrayList<>();
        v.stmtList.addAll(Arrays.asList(stmts));
        return v;
    }

    protected SemValue svBlock(Tree.Block block) {
        var v = new SemValue(SemValue.Kind.BLOCK, block == null ? Pos.NoPos : block.pos);
        v.block = block;
        return v;
    }

    protected SemValue svExpr(Tree.Expr expr) {
        var v = new SemValue(SemValue.Kind.EXPR, expr == null ? Pos.NoPos : expr.pos);
        v.expr = expr;
        return v;
    }

    protected SemValue svExprs(Tree.Expr... exprs) {
        var v = new SemValue(SemValue.Kind.EXPR_LIST, exprs.length == 0 ? Pos.NoPos : exprs[0].pos);
        v.exprList = new ArrayList<>();
        v.exprList.addAll(Arrays.asList(exprs));
        return v;
    }

    protected SemValue svLValue(Tree.LValue lValue) {
        var v = new SemValue(SemValue.Kind.LVALUE, lValue == null ? Pos.NoPos : lValue.pos);
        v.lValue = lValue;
        return v;
    }

    protected SemValue svId(Tree.Id id) {
        var v = new SemValue(SemValue.Kind.ID, id == null ? Pos.NoPos : id.pos);
        v.id = id;
        return v;
    }

    /**
     * Helper method used by the concrete parser: report error.
     *
     * @param msg the error message
     */
    protected void yyerror(String msg) {
        issuer.issue(new MsgError(lexer.getPos(), msg));
    }
}
