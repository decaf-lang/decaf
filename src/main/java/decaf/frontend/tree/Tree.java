package decaf.frontend.tree;

import decaf.frontend.scope.GlobalScope;
import decaf.frontend.scope.LocalScope;
import decaf.frontend.symbol.ClassSymbol;
import decaf.frontend.symbol.MethodSymbol;
import decaf.frontend.symbol.VarSymbol;
import decaf.frontend.type.FunType;
import decaf.frontend.type.Type;
import decaf.lowlevel.instr.Temp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * All kinds of tree node in the abstract syntax tree.
 *
 * @see TreeNode
 */
public abstract class Tree {
    public enum Kind {
        TOP_LEVEL, CLASS_DEF, VAR_DEF, METHOD_DEF,
        T_INT, T_BOOL, T_STRING, T_VOID, T_CLASS, T_ARRAY,
        LOCAL_VAR_DEF, BLOCK, ASSIGN, EXPR_EVAL, SKIP, IF, WHILE, FOR, BREAK, RETURN, PRINT,
        INT_LIT, BOOL_LIT, STRING_LIT, NULL_LIT, VAR_SEL, INDEX_SEL, CALL,
        THIS, UNARY_EXPR, BINARY_EXPR, READ_INT, READ_LINE, NEW_CLASS, NEW_ARRAY, CLASS_TEST, CLASS_CAST
    }

    /**
     * A top-level Decaf program, which consists of many class definitions.
     */
    public static class TopLevel extends TreeNode {
        // Tree elements
        public List<ClassDef> classes;
        // For type check
        public GlobalScope globalScope;
        public ClassSymbol mainClass;

        public TopLevel(List<ClassDef> classes, Pos pos) {
            super(Kind.TOP_LEVEL, "TopLevel", pos);
            this.classes = classes;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> classes;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitTopLevel(this, ctx);
        }
    }

    /**
     * Class definition.
     * <pre>
     *     'class' id {'extends' parent}? '{' fields '}'
     * </pre>
     */
    public static class ClassDef extends TreeNode {
        // Tree elements
        public final Id id;
        public Optional<Id> parent;
        public final List<Field> fields;
        // For convenience
        public final String name;
        // For type check
        public ClassDef superClass;
        public ClassSymbol symbol;
        public boolean resolved = false;

        public ClassDef(Id id, Optional<Id> parent, List<Field> fields, Pos pos) {
            super(Kind.CLASS_DEF, "ClassDef", pos);
            this.id = id;
            this.parent = parent;
            this.fields = fields;
            this.name = id.name;
        }

        public boolean hasParent() {
            return parent.isPresent();
        }

        public List<MethodDef> methods() {
            var methods = new ArrayList<MethodDef>();
            for (var field : fields) {
                if (field instanceof MethodDef) {
                    methods.add((MethodDef) field);
                }
            }
            return methods;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> id;
                case 1 -> parent;
                case 2 -> fields;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 3;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitClassDef(this, ctx);
        }
    }

    /**
     * Field/Member of a class.
     */
    public static abstract class Field extends TreeNode {
        public Field(Kind kind, String displayName, Pos pos) {
            super(kind, displayName, pos);
        }
    }

    /**
     * Member variable declaration.
     * <pre>
     *     type id ';'
     * </pre>
     * Initialization is not supported.
     */
    public static class VarDef extends Field {
        // Tree elements
        public TypeLit typeLit;
        public Id id;
        // For now, member variable must not have initial values, but we need to print this
        public final Optional<Expr> initVal = Optional.empty();
        // For convenience
        public String name;
        // For type check
        public VarSymbol symbol;

        public VarDef(TypeLit typeLit, Id id, Pos pos) {
            super(Kind.VAR_DEF, "VarDef", pos);
            this.typeLit = typeLit;
            this.id = id;
            this.name = id.name;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> typeLit;
                case 1 -> id;
                case 2 -> initVal;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 3;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitVarDef(this, ctx);
        }
    }

    /**
     * Member method definition.
     * <pre>
     *     'static'? returnType id '(' type1 id1 ',' type2 id2 ',' ... ')' '{' body '}'
     * </pre>
     * Decaf has static methods but NO static variables, strange!
     */
    public static class MethodDef extends Field {
        // Tree elements
        public Modifiers modifiers;
        public Id id;
        public TypeLit returnType;
        public List<LocalVarDef> params;
        public Block body;
        // For convenience
        public String name;
        // For type check
        public FunType type;
        public MethodSymbol symbol;

        public MethodDef(boolean isStatic, Id id, TypeLit returnType, List<LocalVarDef> params, Block body, Pos pos) {
            super(Kind.METHOD_DEF, "MethodDef", pos);
            this.modifiers = isStatic ? new Modifiers(Modifiers.STATIC, pos) : new Modifiers();
            this.id = id;
            this.returnType = returnType;
            this.params = params;
            this.body = body;
            this.name = id.name;
        }

        public boolean isStatic() {
            return modifiers.isStatic();
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> modifiers;
                case 1 -> id;
                case 2 -> returnType;
                case 3 -> params;
                case 4 -> body;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 5;
        }

        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitMethodDef(this, ctx);
        }
    }

    /**
     * Type.
     * <p>
     * Decaf only supports
     * - basic types (integer, boolean, string, void),
     * - class types (using class identifiers), and
     * - array types (whose element could be any type, but homogeneous).
     */
    public static abstract class TypeLit extends TreeNode {
        public Type type;

        public TypeLit(Kind kind, String displayName, Pos pos) {
            super(kind, displayName, pos);
        }
    }

    /**
     * 32 bit integer type: {@code int}.
     */
    public static class TInt extends TypeLit {
        public TInt(Pos pos) {
            super(Kind.T_INT, "TInt", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitTInt(this, ctx);
        }
    }

    /**
     * Boolean type: {@code bool}.
     */
    public static class TBool extends TypeLit {
        public TBool(Pos pos) {
            super(Kind.T_BOOL, "TBool", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitTBool(this, ctx);
        }
    }

    /**
     * String type: {@code string}.
     */
    public static class TString extends TypeLit {
        public TString(Pos pos) {
            super(Kind.T_STRING, "TString", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitTString(this, ctx);
        }
    }

    /**
     * Void type: {@code void}.
     * <p>
     * For method return type ONLY.
     */
    public static class TVoid extends TypeLit {
        public TVoid(Pos pos) {
            super(Kind.T_VOID, "TVoid", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitTVoid(this, ctx);
        }
    }

    /**
     * Class type.
     * <pre>
     *     'class' id
     * </pre>
     */
    public static class TClass extends TypeLit {
        // Tree element
        public Id id;

        public TClass(Id id, Pos pos) {
            super(Kind.T_CLASS, "TClass", pos);
            this.id = id;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> id;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitTClass(this, ctx);
        }
    }

    /**
     * Array type.
     * <pre>
     *     elemType '[' ']'
     * </pre>
     */
    public static class TArray extends TypeLit {
        // Tree element
        public TypeLit elemType;

        public TArray(TypeLit elemType, Pos pos) {
            super(Kind.T_ARRAY, "TArray", pos);
            this.elemType = elemType;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> elemType;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitTArray(this, ctx);
        }
    }


    /**
     * Statement.
     */
    public static abstract class Stmt extends TreeNode {
        public Stmt(Kind kind, String displayName, Pos pos) {
            super(kind, displayName, pos);
        }

        /**
         * For type check: does this return a value?
         */
        public boolean returns = false;

        public boolean isBlock() {
            return false;
        }
    }

    /**
     * Local variable declaration.
     * <pre>
     *     typeLit id {'=' expr}? ';'
     * </pre>
     * Initialization is optional.
     */
    public static class LocalVarDef extends Stmt {
        // Tree elements
        public TypeLit typeLit;
        public Id id;
        public Pos assignPos;
        public Optional<Expr> initVal;
        // For convenience
        public String name;
        // For type check
        public VarSymbol symbol;

        public LocalVarDef(TypeLit typeLit, Id id, Pos assignPos, Optional<Expr> initVal, Pos pos) {
            // pos = id.pos, assignPos = position of the '='
            // TODO: looks not very consistent, maybe we shall always report error simply at `pos`, not `assignPos`?
            super(Kind.LOCAL_VAR_DEF, "LocalVarDef", pos);
            this.typeLit = typeLit;
            this.id = id;
            this.assignPos = assignPos;
            this.initVal = initVal;
            this.name = id.name;
        }

        public LocalVarDef(TypeLit typeLit, Id id, Pos pos) {
            this(typeLit, id, Pos.NoPos, Optional.empty(), pos);
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> typeLit;
                case 1 -> id;
                case 2 -> initVal;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 3;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitLocalVarDef(this, ctx);
        }
    }

    /**
     * Statement block.
     * <pre>
     *     '{' stmt1 stmt2 ... '}'
     * </pre>
     */
    public static class Block extends Stmt {
        // Tree element
        public List<Stmt> stmts;
        // For type check
        public LocalScope scope;

        public Block(List<Stmt> stmts, Pos pos) {
            super(Kind.BLOCK, "Block", pos);
            this.stmts = stmts;
        }

        @Override
        public boolean isBlock() {
            return true;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> stmts;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitBlock(this, ctx);
        }
    }

    /**
     * Assignment.
     * <pre>
     *     lhs = rhs;
     * </pre>
     * Note the left-hand side must be a {@link LValue}.
     */
    public static class Assign extends Stmt {
        // Tree elements
        public LValue lhs;
        public Expr rhs;

        public Assign(LValue lhs, Expr rhs, Pos pos) {
            super(Kind.ASSIGN, "Assign", pos);
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> lhs;
                case 1 -> rhs;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 2;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitAssign(this, ctx);
        }
    }

    /**
     * Expression evaluation, typically a method call.
     */
    public static class ExprEval extends Stmt {
        // Tree element
        public Expr expr;

        public ExprEval(Expr expr, Pos pos) {
            super(Kind.EXPR_EVAL, "ExprEval", pos);
            this.expr = expr;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> expr;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitExprEval(this, ctx);
        }
    }

    /**
     * Empty statement, do nothing.
     */
    public static class Skip extends Stmt {

        public Skip(Pos pos) {
            super(Kind.SKIP, "Skip", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitSkip(this, ctx);
        }
    }

    /**
     * Wrap a single statement as a block. In case the statement itself is already a block, then return itself.
     * <p>
     * Why? To ease the analysis of statements that naturally opens a local scope, like loop bodies and condition
     * branches, we represent them each as a {@link Block} in the abstract syntax tree level. However, if it contains a
     * single statement, like the following:
     * <pre>
     *     if (true) return 1;
     * </pre>
     * It do obey the syntax but NOT fit our tree node. In this case, we simply wrap it as a block:
     * <pre>
     *     if (true) { return 1; }
     * </pre>
     *
     * @param stmt a statement
     * @return the wrapped block
     */
    private static Block blocked(Stmt stmt) {
        if (stmt.isBlock()) return (Block) stmt;
        var ss = new ArrayList<Stmt>();
        ss.add(stmt);
        return new Block(ss, stmt.pos);
    }

    /**
     * If statement.
     * <pre>
     *     if '(' cond ')' trueBranch {'else' falseBranch}?
     * </pre>
     * False branch is optional.
     */
    public static class If extends Stmt {
        // Tree elements
        public Expr cond;
        public Block trueBranch;
        public Optional<Block> falseBranch;

        public If(Expr cond, Stmt trueBranch, Optional<Stmt> falseBranch, Pos pos) {
            super(Kind.IF, "If", pos);
            this.cond = cond;
            this.trueBranch = blocked(trueBranch);
            this.falseBranch = falseBranch.map(Tree::blocked);
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> cond;
                case 1 -> trueBranch;
                case 2 -> falseBranch;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 3;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitIf(this, ctx);
        }
    }

    /**
     * While statement.
     * <pre>
     *     'while' '(' cond ')' body
     * </pre>
     */
    public static class While extends Stmt {
        // Tree elements
        public Expr cond;
        public Block body;

        public While(Expr cond, Stmt body, Pos pos) {
            super(Kind.WHILE, "While", pos);
            this.cond = cond;
            this.body = blocked(body);
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> cond;
                case 1 -> body;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 2;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitWhile(this, ctx);
        }
    }

    /**
     * For statement.
     * <pre>
     *     'for' '(' init ';' cond ';' update ')' body
     * </pre>
     */
    public static class For extends Stmt {
        // Tree elements
        public Stmt init; // In syntax, this is limited to a simple statement.
        public Expr cond;
        public Stmt update; // In syntax, this is limited to a simple statement.
        public Block body;
        // For type check
        public LocalScope scope;

        public For(Stmt init, Expr cond, Stmt update, Stmt body, Pos pos) {
            super(Kind.FOR, "For", pos);
            Objects.requireNonNull(init);
            Objects.requireNonNull(update);
            this.init = init;
            this.cond = cond;
            this.update = update;
            this.body = blocked(body);
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> init;
                case 1 -> cond;
                case 2 -> update;
                case 3 -> body;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 4;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitFor(this, ctx);
        }
    }

    /**
     * Break statement.
     * <pre>
     *     break;
     * </pre>
     * Jump out of the <em>innermost</em> loop.
     */
    public static class Break extends Stmt {

        public Break(Pos pos) {
            super(Kind.BREAK, "Break", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitBreak(this, ctx);
        }
    }

    /**
     * Return statement.
     * <pre>
     *     return {expr}? ';'
     * </pre>
     * If the expression is none, then we say its return type is void.
     */
    public static class Return extends Stmt {
        // Tree elements
        public Optional<Expr> expr;

        public Return(Optional<Expr> expr, Pos pos) {
            super(Kind.RETURN, "Return", pos);
            this.expr = expr;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> expr;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitReturn(this, ctx);
        }
    }

    /**
     * Print statement.
     * <pre>
     *     'Print' '(' expr1 ',' expr2 ',' ... ')' ';'
     * </pre>
     * ONLY print integers, booleans and strings.
     */
    public static class Print extends Stmt {
        // Tree elements
        public List<Expr> exprs;

        public Print(List<Expr> exprs, Pos pos) {
            super(Kind.PRINT, "Print", pos);
            this.exprs = exprs;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> exprs;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitPrint(this, ctx);
        }
    }

    /**
     * Expression.
     */
    public abstract static class Expr extends TreeNode {
        // For type check
        public Type type;
        // For tac gen
        public Temp val;

        public Expr(Kind kind, String displayName, Pos pos) {
            super(kind, displayName, pos);
        }
    }

    /**
     * Integer literal.
     */
    public static class IntLit extends Expr {
        // Tree element
        public int value;

        public IntLit(int value, Pos pos) {
            super(Kind.INT_LIT, "IntLit", pos);
            this.value = value;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> value;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitIntLit(this, ctx);
        }
    }

    /**
     * Boolean literal.
     */
    public static class BoolLit extends Expr {
        // Tree element
        public boolean value;

        public BoolLit(boolean value, Pos pos) {
            super(Kind.BOOL_LIT, "BoolLit", pos);
            this.value = value;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> value;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitBoolLit(this, ctx);
        }
    }

    /**
     * String literal.
     */
    public static class StringLit extends Expr {
        // Tree element
        public String value; // the quoted string

        public StringLit(String value, Pos pos) {
            super(Kind.STRING_LIT, "StringLit", pos);
            this.value = value;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> value;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitStringLit(this, ctx);
        }
    }

    /**
     * Null literal: {@code null}.
     */
    public static class NullLit extends Expr {
        // Tree element
        public final Object value = null;

        public NullLit(Pos pos) {
            super(Kind.NULL_LIT, "NullLit", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitNullLit(this, ctx);
        }
    }

    /**
     * Left value, i.e. an expression that can be assigned to a value.
     */
    public static abstract class LValue extends Expr {
        public LValue(Kind kind, String displayName, Pos pos) {
            super(kind, displayName, pos);
        }
    }

    /**
     * Field selection, or simply a local variable.
     * <pre>
     *     {receiver '.'}? field
     * </pre>
     */
    public static class VarSel extends LValue {
        // Tree element
        public Optional<Expr> receiver;
        public Id variable;
        // For convenience
        public String name;
        // For type check
        public VarSymbol symbol;
        public boolean isClassName = false;

        public VarSel(Optional<Expr> receiver, Id variable, Pos pos) {
            super(Kind.VAR_SEL, "VarSel", pos);
            this.receiver = receiver;
            this.variable = variable;
            this.name = variable.name;
        }

        public VarSel(Expr receiver, Id variable, Pos pos) {
            this(Optional.of(receiver), variable, pos);
        }

        public VarSel(Id variable, Pos pos) {
            this(Optional.empty(), variable, pos);
        }

        /**
         * Set its receiver as {@code this}.
         * <p>
         * Reversed for type check.
         */
        public void setThis() {
            if (receiver.isEmpty()) {
                receiver = Optional.of(new Tree.This(pos));
            }
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> receiver;
                case 1 -> variable;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 2;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitVarSel(this, ctx);
        }
    }


    /**
     * Array element selection by index.
     * <pre>
     *     array '[' index ']'
     * </pre>
     */
    public static class IndexSel extends LValue {

        public Expr array;
        public Expr index;

        public IndexSel(Expr array, Expr index, Pos pos) {
            super(Kind.INDEX_SEL, "IndexSel", pos);
            this.array = array;
            this.index = index;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> array;
                case 1 -> this.index;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 2;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitIndexSel(this, ctx);
        }
    }

    /**
     * This expression: {@code this}.
     * <p>
     * Refers to the instance of the current class.
     */
    public static class This extends Expr {

        public This(Pos pos) {
            super(Kind.THIS, "This", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> visitor, C ctx) {
            visitor.visitThis(this, ctx);
        }
    }

    public enum UnaryOp {
        NEG, NOT
    }

    /**
     * Get the string representation of an unary operator.
     *
     * @param op operator
     * @return string representation
     */
    public static String opStr(UnaryOp op) {
        return switch (op) {
            case NEG -> "-";
            case NOT -> "!";
        };
    }

    /**
     * Unary expression.
     */
    public static class Unary extends Expr {

        public UnaryOp op;
        public Expr operand;

        public Unary(UnaryOp op, Expr operand, Pos pos) {
            super(Kind.UNARY_EXPR, "Unary", pos);
            this.op = op;
            this.operand = operand;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> op;
                case 1 -> operand;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 2;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitUnary(this, ctx);
        }
    }

    public enum BinaryOp {
        ADD, SUB, MUL, DIV, MOD,
        EQ, NE, GE, GT, LE, LT,
        AND, OR
    }

    /**
     * Get the string representation of a binary operator.
     *
     * @param op operator
     * @return string representation
     */
    public static String opStr(BinaryOp op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case MOD -> "%";
            case EQ -> "==";
            case NE -> "!=";
            case GE -> ">=";
            case GT -> ">";
            case LE -> "<=";
            case LT -> "<";
            case AND -> "&&";
            case OR -> "||";
        };
    }

    /**
     * Binary expression.
     */
    public static class Binary extends Expr {

        public BinaryOp op;
        public Expr lhs;
        public Expr rhs;

        public Binary(BinaryOp op, Expr lhs, Expr rhs, Pos pos) {
            super(Kind.BINARY_EXPR, "Binary", pos);
            this.op = op;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> op;
                case 1 -> lhs;
                case 2 -> rhs;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 3;
        }

        @Override
        public <C> void accept(Visitor<C> visitor, C ctx) {
            visitor.visitBinary(this, ctx);
        }
    }

    /**
     * IO expression for reading an integer from stdin: {@code ReadInteger()}.
     */
    public static class ReadInt extends Expr {

        public ReadInt(Pos pos) {
            super(Kind.READ_INT, "ReadInt", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> visitor, C ctx) {
            visitor.visitReadInt(this, ctx);
        }
    }

    /**
     * IO expression for reading a line from stdin: {@code ReadLine()}.
     */
    public static class ReadLine extends Expr {

        public ReadLine(Pos pos) {
            super(Kind.READ_LINE, "ReadLine", pos);
        }

        @Override
        public Object treeElementAt(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int treeArity() {
            return 0;
        }

        @Override
        public <C> void accept(Visitor<C> visitor, C ctx) {
            visitor.visitReadLine(this, ctx);
        }
    }


    /**
     * New expression for creating an instance.
     * <pre>
     *     'new' id '(' ')'
     * </pre>
     * Currently, no arguments are allowed.
     */
    public static class NewClass extends Expr {
        // Tree elements
        public Id clazz;
        // For type check
        public ClassSymbol symbol;

        public NewClass(Id clazz, Pos pos) {
            super(Kind.NEW_CLASS, "NewClass", pos);
            this.clazz = clazz;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> clazz;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 1;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitNewClass(this, ctx);
        }
    }

    /**
     * New expression for creating an array.
     * <pre>
     *     'new' elemType '[' length ']'
     * </pre>
     */
    public static class NewArray extends Expr {
        // Tree elements
        public TypeLit elemType;
        public Expr length;

        public NewArray(TypeLit elemType, Expr length, Pos pos) {
            super(Kind.NEW_ARRAY, "NewArray", pos);
            this.elemType = elemType;
            this.length = length;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> elemType;
                case 1 -> length;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 2;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitNewArray(this, ctx);
        }
    }

    /**
     * Instance-of expression.
     * <pre>
     *     'instanceof' '(' obj ','  is ')'
     * </pre>
     * Check if the given object {@code obj} is an instance of class {@code is}.
     */
    public static class ClassTest extends Expr {
        // Tree elements
        public Expr obj;
        public Id is;
        // For type check
        public ClassSymbol symbol;

        public ClassTest(Expr obj, Id is, Pos pos) {
            super(Kind.CLASS_TEST, "ClassTest", pos);
            this.obj = obj;
            this.is = is;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> obj;
                case 1 -> is;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 2;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitClassTest(this, ctx);
        }
    }

    /**
     * Class type cast expression.
     * <pre>
     *     '(' 'class' to ')' obj
     * </pre>
     * Cast the given object {@code obj} into class type {@code to}.
     */
    public static class ClassCast extends Expr {
        // Tree elements
        public Expr obj;
        public Id to;
        // For type check
        public ClassSymbol symbol;

        public ClassCast(Expr obj, Id to, Pos pos) {
            super(Kind.CLASS_CAST, "ClassCast", pos);
            this.obj = obj;
            this.to = to;
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> obj;
                case 1 -> to;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 2;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitClassCast(this, ctx);
        }
    }


    /**
     * Call expression.
     * <pre>
     *     {receiver '.'}? id '(' arg1 ',' arg2 ',' ... ')'
     * </pre>
     */
    public static class Call extends Expr {
        // Tree elements
        public Optional<Expr> receiver;
        public Id method;
        public List<Expr> args;
        //
        public String methodName;
        // For type check
        public MethodSymbol symbol;
        public boolean isArrayLength = false;

        public Call(Optional<Expr> receiver, Id method, List<Expr> args, Pos pos) {
            super(Kind.CALL, "Call", pos);
            this.receiver = receiver;
            this.method = method;
            this.args = args;
            this.methodName = method.name;
        }

        public Call(Id method, List<Expr> args, Pos pos) {
            this(Optional.empty(), method, args, pos);
        }

        public Call(Expr receiver, Id method, List<Expr> args, Pos pos) {
            this(Optional.of(receiver), method, args, pos);
        }

        /**
         * Set its receiver as {@code this}.
         * <p>
         * Reversed for type check.
         */
        public void setThis() {
            this.receiver = Optional.of(new This(pos));
        }

        @Override
        public Object treeElementAt(int index) {
            return switch (index) {
                case 0 -> receiver;
                case 1 -> method;
                case 2 -> args;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int treeArity() {
            return 3;
        }

        @Override
        public <C> void accept(Visitor<C> v, C ctx) {
            v.visitCall(this, ctx);
        }
    }

    /**
     * An identifier.
     */
    public static class Id {
        public final String name;

        public final Pos pos;

        public Id(String name, Pos pos) {
            this.name = name;
            this.pos = pos;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Modifiers.
     * <p>
     * Modifiers are encoded as an integer, whose binary representation reveals which modifiers are enabled. In this
     * way, you can use {@code +} or {@code |} to enable multiple modifiers, like we do in system programming.
     * <p>
     * In particular, the original Decaf language only has one modifier -- static. If a method is static, then the
     * lowest bit is set.
     */
    public static class Modifiers {
        public final int code;

        public final Pos pos;

        private List<String> flags;

        // Available modifiers:
        public static final int STATIC = 1;

        public Modifiers(int code, Pos pos) {
            this.code = code;
            this.pos = pos;
            flags = new ArrayList<>();
            if (isStatic()) flags.add("STATIC");
        }

        public Modifiers() {
            this(0, Pos.NoPos);
        }

        public boolean isStatic() {
            return (code & 1) == 1;
        }

        @Override
        public String toString() {
            return String.join(" ", flags);
        }
    }
}
