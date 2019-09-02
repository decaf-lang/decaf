package decaf.typecheck;

import decaf.Driver;
import decaf.error.*;
import decaf.frontend.Parser;
import decaf.scope.ClassScope;
import decaf.scope.FormalScope;
import decaf.scope.Scope;
import decaf.scope.Scope.Kind;
import decaf.scope.ScopeStack;
import decaf.symbol.Class;
import decaf.symbol.Function;
import decaf.symbol.Symbol;
import decaf.symbol.Variable;
import decaf.tree.Pos;
import decaf.tree.Tree;
import decaf.type.ArrayType;
import decaf.type.BaseType;
import decaf.type.ClassType;
import decaf.type.Type;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class TypeCheck extends Tree.Visitor {

    private ScopeStack table;

    private Stack<Tree.Stmt> breaks;

    private Function currentFunction;

    public TypeCheck(ScopeStack table) {
        this.table = table;
        breaks = new Stack<Tree.Stmt>();
    }

    public static void checkType(Tree.TopLevel tree) {
        new TypeCheck(Driver.getDriver().getTable()).visitTopLevel(tree);
    }

    @Override
    public void visitBinary(Tree.Binary expr) {
        expr.type = checkBinaryOp(expr.lhs, expr.rhs, expr.op, expr.pos);
    }

    @Override
    public void visitUnary(Tree.Unary expr) {
        expr.operand.accept(this);
        if (expr.op == Tree.UnaryOp.NEG) {
            if (expr.operand.type.equal(BaseType.ERROR)
                    || expr.operand.type.equal(BaseType.INT)) {
                expr.type = expr.operand.type;
            } else {
                issueError(new IncompatUnOpError(expr.getLocation(), "-",
                        expr.operand.type.toString()));
                expr.type = BaseType.ERROR;
            }
        } else {
            if (!(expr.operand.type.equal(BaseType.BOOL) || expr.operand.type
                    .equal(BaseType.ERROR))) {
                issueError(new IncompatUnOpError(expr.getLocation(), "!",
                        expr.operand.type.toString()));
            }
            expr.type = BaseType.BOOL;
        }
    }

    @Override
    public void visitIntLit(Tree.IntLit that) {
        that.type = BaseType.INT;
    }

    @Override
    public void visitBoolLit(Tree.BoolLit that) {
        that.type = BaseType.BOOL;
    }

    @Override
    public void visitStringLit(Tree.StringLit that) {
        that.type = BaseType.STRING;
    }

    @Override
    public void visitNullLit(Tree.NullLit that) {
        that.type = BaseType.NULL;
    }

    @Override
    public void visitReadInt(Tree.ReadInt readInt) {
        readInt.type = BaseType.INT;
    }

    @Override
    public void visitReadLine(Tree.ReadLine readStringExpr) {
        readStringExpr.type = BaseType.STRING;
    }

    @Override
    public void visitIndexSel(Tree.IndexSel indexed) {
        indexed.lvKind = Tree.LValue.LVKind.ARRAY_ELEMENT;
        indexed.array.accept(this);
        if (!indexed.array.type.isArrayType()) {
            issueError(new NotArrayError(indexed.array.getLocation()));
            indexed.type = BaseType.ERROR;
        } else {
            indexed.type = ((ArrayType) indexed.array.type)
                    .getElementType();
        }
        indexed.index.accept(this);
        if (!indexed.index.type.equal(BaseType.INT)) {
            issueError(new SubNotIntError(indexed.getLocation()));
        }
    }

    private void checkCallExpr(Tree.Call callExpr, Symbol f) {
        Type receiverType = callExpr.receiver.isEmpty() ? ((ClassScope) table
                .lookForScope(Scope.Kind.CLASS)).getOwner().getType()
                : callExpr.receiver.get().type;
        if (f == null) {
            issueError(new FieldNotFoundError(callExpr.getLocation(),
                    callExpr.method.name, receiverType.toString()));
            callExpr.type = BaseType.ERROR;
        } else if (!f.isFunction()) {
            issueError(new NotClassMethodError(callExpr.getLocation(),
                    callExpr.method.name, receiverType.toString()));
            callExpr.type = BaseType.ERROR;
        } else {
            Function func = (Function) f;
            callExpr.symbol = func;
            callExpr.type = func.getReturnType();
            if (callExpr.receiver.isEmpty() && currentFunction.isStatik()
                    && !func.isStatik()) {
                issueError(new RefNonStaticError(callExpr.getLocation(),
                        currentFunction.getName(), func.getName()));
            }
            if (!func.isStatik() && callExpr.receiver.isPresent()
                    && callExpr.receiver.get().isClass) {
                issueError(new NotClassFieldError(callExpr.getLocation(),
                        callExpr.method.name, callExpr.receiver.get().type.toString()));
            }
            if (func.isStatik()) {
                callExpr.receiver = Optional.empty();
            } else {
                if (callExpr.receiver.isEmpty() && !currentFunction.isStatik()) {
                    callExpr.receiver = Optional.of(new Tree.This(callExpr.getLocation()));
                    callExpr.receiver.get().accept(this);
                }
            }
            for (Tree.Expr e : callExpr.args) {
                e.accept(this);
            }
            List<Type> argList = func.getType().getArgList();
            int argCount = func.isStatik() ? callExpr.args.size()
                    : callExpr.args.size() + 1;
            if (argList.size() != argCount) {
                issueError(new BadArgCountError(callExpr.getLocation(),
                        callExpr.method.name, func.isStatik() ? argList.size()
                        : argList.size() - 1, callExpr.args.size()));
            } else {
                Iterator<Type> iter1 = argList.iterator();
                if (!func.isStatik()) {
                    iter1.next();
                }
                Iterator<Tree.Expr> iter2 = callExpr.args.iterator();
                for (int i = 1; iter1.hasNext(); i++) {
                    Type t1 = iter1.next();
                    Tree.Expr e = iter2.next();
                    Type t2 = e.type;
                    if (!t2.equal(BaseType.ERROR) && !t2.compatible(t1)) {
                        issueError(new BadArgTypeError(e.getLocation(), i,
                                t2.toString(), t1.toString()));
                    }
                }
            }
        }
    }

    // FIXME: too many .get() in this class, very ugly!

    @Override
    public void visitCall(Tree.Call callExpr) {
        if (callExpr.receiver.isEmpty()) {
            ClassScope cs = (ClassScope) table.lookForScope(Kind.CLASS);
            checkCallExpr(callExpr, cs.lookupVisible(callExpr.method.name));
            return;
        }
        callExpr.receiver.get().usedForRef = true;
        callExpr.receiver.get().accept(this);
        if (callExpr.receiver.get().type.equal(BaseType.ERROR)) {
            callExpr.type = BaseType.ERROR;
            return;
        }
        if (callExpr.method.equals("length")) {
            if (callExpr.receiver.get().type.isArrayType()) {
                if (callExpr.args.size() > 0) {
                    issueError(new BadLengthArgError(callExpr.getLocation(),
                            callExpr.args.size()));
                }
                callExpr.type = BaseType.INT;
                callExpr.isArrayLength = true;
                return;
            } else if (!callExpr.receiver.get().type.isClassType()) {
                issueError(new BadLengthError(callExpr.getLocation()));
                callExpr.type = BaseType.ERROR;
                return;
            }
        }

        if (!callExpr.receiver.get().type.isClassType()) {
            issueError(new NotClassFieldError(callExpr.getLocation(),
                    callExpr.method.name, callExpr.receiver.get().type.toString()));
            callExpr.type = BaseType.ERROR;
            return;
        }

        ClassScope cs = ((ClassType) callExpr.receiver.get().type)
                .getClassScope();
        checkCallExpr(callExpr, cs.lookupVisible(callExpr.method.name));
    }

    @Override
    public void visitExprEval(Tree.ExprEval exec) {
        exec.expr.accept(this);
    }

    @Override
    public void visitNewArray(Tree.NewArray newArrayExpr) {
        newArrayExpr.elemType.accept(this);
        if (newArrayExpr.elemType.type.equal(BaseType.ERROR)) {
            newArrayExpr.type = BaseType.ERROR;
        } else if (newArrayExpr.elemType.type.equal(BaseType.VOID)) {
            issueError(new BadArrElementError(newArrayExpr.elemType
                    .getLocation()));
            newArrayExpr.type = BaseType.ERROR;
        } else {
            newArrayExpr.type = new ArrayType(
                    newArrayExpr.elemType.type);
        }
        newArrayExpr.length.accept(this);
        if (!newArrayExpr.length.type.equal(BaseType.ERROR)
                && !newArrayExpr.length.type.equal(BaseType.INT)) {
            issueError(new BadNewArrayLength(newArrayExpr.length.getLocation()));
        }
    }

    @Override
    public void visitNewClass(Tree.NewClass newClass) {
        Class c = table.lookupClass(newClass.clazz.name);
        newClass.symbol = c;
        if (c == null) {
            issueError(new ClassNotFoundError(newClass.getLocation(),
                    newClass.clazz.name));
            newClass.type = BaseType.ERROR;
        } else {
            newClass.type = c.getType();
        }
    }

    @Override
    public void visitThis(Tree.This thisExpr) {
        if (currentFunction.isStatik()) {
            issueError(new ThisInStaticFuncError(thisExpr.getLocation()));
            thisExpr.type = BaseType.ERROR;
        } else {
            thisExpr.type = ((ClassScope) table.lookForScope(Scope.Kind.CLASS))
                    .getOwner().getType();
        }
    }

    @Override
    public void visitClassTest(Tree.ClassTest instanceofExpr) {
        instanceofExpr.obj.accept(this);
        if (!instanceofExpr.obj.type.isClassType()) {
            issueError(new NotClassError(instanceofExpr.obj.type
                    .toString(), instanceofExpr.getLocation()));
        }
        Class c = table.lookupClass(instanceofExpr.is.name);
        instanceofExpr.symbol = c;
        instanceofExpr.type = BaseType.BOOL;
        if (c == null) {
            issueError(new ClassNotFoundError(instanceofExpr.getLocation(),
                    instanceofExpr.is.name));
        }
    }

    @Override
    public void visitClassCast(Tree.ClassCast cast) {
        cast.obj.accept(this);
        if (!cast.obj.type.isClassType()) {
            issueError(new NotClassError(cast.obj.type.toString(),
                    cast.getLocation()));
        }
        Class c = table.lookupClass(cast.to.name);
        cast.symbol = c;
        if (c == null) {
            issueError(new ClassNotFoundError(cast.getLocation(),
                    cast.to.name));
            cast.type = BaseType.ERROR;
        } else {
            cast.type = c.getType();
        }
    }

    @Override
    public void visitVarSel(Tree.VarSel varSel) {
        if (varSel.receiver.isEmpty()) {
            Symbol v = table.lookupBeforeLocation(varSel.variable.name, varSel
                    .getLocation());
            if (v == null) {
                issueError(new UndeclVarError(varSel.getLocation(), varSel.variable.name));
                varSel.type = BaseType.ERROR;
            } else if (v.isVariable()) {
                Variable var = (Variable) v;
                varSel.type = var.getType();
                varSel.symbol = var;
                if (var.isLocalVar()) {
                    varSel.lvKind = Tree.LValue.LVKind.LOCAL_VAR;
                } else if (var.isParam()) {
                    varSel.lvKind = Tree.LValue.LVKind.PARAM_VAR;
                } else {
                    if (currentFunction.isStatik()) {
                        issueError(new RefNonStaticError(varSel.getLocation(),
                                currentFunction.getName(), varSel.variable.name));
                    } else {
                        varSel.receiver = Optional.of(new Tree.This(varSel.getLocation()));
                        varSel.receiver.get().accept(this);
                    }
                    varSel.lvKind = Tree.LValue.LVKind.MEMBER_VAR;
                }
            } else {
                varSel.type = v.getType();
                if (v.isClass()) {
                    if (varSel.usedForRef) {
                        varSel.isClass = true;
                    } else {
                        issueError(new UndeclVarError(varSel.getLocation(),
                                varSel.variable.name));
                        varSel.type = BaseType.ERROR;
                    }

                }
            }
        } else {
            varSel.receiver.get().usedForRef = true;
            varSel.receiver.get().accept(this);
            if (!varSel.receiver.get().type.equal(BaseType.ERROR)) {
                if (varSel.receiver.get().isClass || !varSel.receiver.get().type.isClassType()) {
                    issueError(new NotClassFieldError(varSel.getLocation(),
                            varSel.variable.name, varSel.receiver.get().type.toString()));
                    varSel.type = BaseType.ERROR;
                } else {
                    ClassScope cs = ((ClassType) varSel.receiver.get().type)
                            .getClassScope();
                    Symbol v = cs.lookupVisible(varSel.variable.name);
                    if (v == null) {
                        issueError(new FieldNotFoundError(varSel.getLocation(),
                                varSel.variable.name, varSel.receiver.get().type.toString()));
                        varSel.type = BaseType.ERROR;
                    } else if (v.isVariable()) {
                        ClassType thisType = ((ClassScope) table
                                .lookForScope(Scope.Kind.CLASS)).getOwner()
                                .getType();
                        varSel.type = v.getType();
                        if (!thisType.compatible(varSel.receiver.get().type)) {
                            issueError(new FieldNotAccessError(varSel
                                    .getLocation(), varSel.variable.name,
                                    varSel.receiver.get().type.toString()));
                        } else {
                            varSel.symbol = (Variable) v;
                            varSel.lvKind = Tree.LValue.LVKind.MEMBER_VAR;
                        }
                    } else {
                        varSel.type = v.getType();
                    }
                }
            } else {
                varSel.type = BaseType.ERROR;
            }
        }
    }

    @Override
    public void visitClassDef(Tree.ClassDef classDef) {
        table.open(classDef.symbol.getAssociatedScope());
        for (var f : classDef.fields) {
            f.accept(this);
        }
        table.close();
    }

    @Override
    public void visitMethodDef(Tree.MethodDef func) {
        this.currentFunction = func.symbol;
        table.open(func.symbol.getAssociatedScope());
        func.body.accept(this);
        table.close();
    }

    @Override
    public void visitTopLevel(Tree.TopLevel program) {
        table.open(program.globalScope);
        for (Tree.ClassDef cd : program.classes) {
            cd.accept(this);
        }
        table.close();
    }

    @Override
    public void visitBlock(Tree.Block block) {
        table.open(block.associatedScope);
        for (var s : block.block) {
            s.accept(this);
        }
        table.close();
    }

    @Override
    public void visitAssign(Tree.Assign assign) {
        assign.lhs.accept(this);
        assign.rhs.accept(this);
        if (!assign.lhs.type.equal(BaseType.ERROR)
                && (assign.lhs.type.isFuncType() || !assign.rhs.type
                .compatible(assign.lhs.type))) {
            issueError(new IncompatBinOpError(assign.getLocation(),
                    assign.lhs.type.toString(), "=", assign.rhs.type
                    .toString()));
        }
    }

    @Override
    public void visitBreak(Tree.Break breakStmt) {
        if (breaks.empty()) {
            issueError(new BreakOutOfLoopError(breakStmt.getLocation()));
        }
    }

    @Override
    public void visitFor(Tree.For forLoop) {

        forLoop.init.accept(this);
        checkTestExpr(forLoop.cond);
        forLoop.update.accept(this);
        breaks.add(forLoop);
        forLoop.body.accept(this);
        breaks.pop();
    }

    @Override
    public void visitIf(Tree.If ifStmt) {
        checkTestExpr(ifStmt.cond);
        ifStmt.trueBranch.accept(this);
        ifStmt.falseBranch.ifPresent(b -> b.accept(this));
    }

    @Override
    public void visitPrint(Tree.Print printStmt) {
        int i = 0;
        for (Tree.Expr e : printStmt.exprs) {
            e.accept(this);
            i++;
            if (!e.type.equal(BaseType.ERROR) && !e.type.equal(BaseType.BOOL)
                    && !e.type.equal(BaseType.INT)
                    && !e.type.equal(BaseType.STRING)) {
                issueError(new BadPrintArgError(e.getLocation(), Integer
                        .toString(i), e.type.toString()));
            }
        }
    }

    @Override
    public void visitReturn(Tree.Return returnStmt) {
        Type returnType = ((FormalScope) table
                .lookForScope(Scope.Kind.FORMAL)).getOwner().getReturnType();
        returnStmt.expr.ifPresent(e -> e.accept(this));
        if (returnType.equal(BaseType.VOID)) {
            if (returnStmt.expr.isPresent()) {
                issueError(new BadReturnTypeError(returnStmt.getLocation(),
                        returnType.toString(), returnStmt.expr.get().type.toString()));
            }
        } else if (returnStmt.expr.isEmpty()) {
            issueError(new BadReturnTypeError(returnStmt.getLocation(),
                    returnType.toString(), "void"));
        } else if (!returnStmt.expr.get().type.equal(BaseType.ERROR)
                && !returnStmt.expr.get().type.compatible(returnType)) {
            issueError(new BadReturnTypeError(returnStmt.getLocation(),
                    returnType.toString(), returnStmt.expr.get().type.toString()));
        }
    }

    @Override
    public void visitWhile(Tree.While aWhile) {
        checkTestExpr(aWhile.cond);
        breaks.add(aWhile);
        aWhile.body.accept(this);
        breaks.pop();
    }

    // visiting types FIXME: same with BuildSym
    @Override
    public void visitTInt(Tree.TInt that) {
        that.type = BaseType.INT;
    }

    @Override
    public void visitTBool(Tree.TBool that) {
        that.type = BaseType.BOOL;
    }

    @Override
    public void visitTString(Tree.TString that) {
        that.type = BaseType.STRING;
    }

    @Override
    public void visitTVoid(Tree.TVoid that) {
        that.type = BaseType.VOID;
    }

    @Override
    public void visitTClass(Tree.TClass typeClass) {
        Class c = table.lookupClass(typeClass.id.name);
        if (c == null) {
            issueError(new ClassNotFoundError(typeClass.getLocation(),
                    typeClass.id.name));
            typeClass.type = BaseType.ERROR;
        } else {
            typeClass.type = c.getType();
        }
    }

    @Override
    public void visitTArray(Tree.TArray typeArray) {
        typeArray.elemType.accept(this);
        if (typeArray.elemType.type.equal(BaseType.ERROR)) {
            typeArray.type = BaseType.ERROR;
        } else if (typeArray.elemType.type.equal(BaseType.VOID)) {
            issueError(new BadArrElementError(typeArray.getLocation()));
            typeArray.type = BaseType.ERROR;
        } else {
            typeArray.type = new decaf.type.ArrayType(
                    typeArray.elemType.type);
        }
    }

    private void issueError(DecafError error) {
        Driver.getDriver().issueError(error);
    }

    private Type checkBinaryOp(Tree.Expr left, Tree.Expr right, Tree.BinaryOp op, Pos pos) {
        left.accept(this);
        right.accept(this);

        if (left.type.equal(BaseType.ERROR) || right.type.equal(BaseType.ERROR)) {
            switch (op) {
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                    return left.type;
                case MOD:
                    return BaseType.INT;
                default:
                    return BaseType.BOOL;
            }
        }

        boolean compatible = false;
        Type returnType = BaseType.ERROR;
        switch (op) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                compatible = left.type.equals(BaseType.INT)
                        && left.type.equal(right.type);
                returnType = left.type;
                break;
            case GT:
            case GE:
            case LT:
            case LE:
                compatible = left.type.equal(BaseType.INT)
                        && left.type.equal(right.type);
                returnType = BaseType.BOOL;
                break;
            case MOD:
                compatible = left.type.equal(BaseType.INT)
                        && right.type.equal(BaseType.INT);
                returnType = BaseType.INT;
                break;
            case EQ:
            case NE:
                compatible = left.type.compatible(right.type)
                        || right.type.compatible(left.type);
                returnType = BaseType.BOOL;
                break;
            case AND:
            case OR:
                compatible = left.type.equal(BaseType.BOOL)
                        && right.type.equal(BaseType.BOOL);
                returnType = BaseType.BOOL;
                break;
            default:
                break;
        }

        if (!compatible) {
            issueError(new IncompatBinOpError(pos, left.type.toString(),
                    Parser.opStr(op), right.type.toString()));
        }
        return returnType;
    }

    private void checkTestExpr(Tree.Expr expr) {
        expr.accept(this);
        if (!expr.type.equal(BaseType.ERROR) && !expr.type.equal(BaseType.BOOL)) {
            issueError(new BadTestExpr(expr.getLocation()));
        }
    }

}
