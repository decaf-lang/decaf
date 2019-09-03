package decaf.translate;

import decaf.backend.OffsetCounter;
import decaf.machdesc.Intrinsic;
import decaf.symbol.Variable;
import decaf.tac.Label;
import decaf.tac.Temp;
import decaf.tree.Tree;
import decaf.tree.Visitor;
import decaf.type.BaseType;

import java.util.Stack;

public class TransPass2 implements Visitor {

    private Translater tr;

    private Temp currentThis;

    private Stack<Label> loopExits;

    public TransPass2(Translater tr) {
        this.tr = tr;
        loopExits = new Stack<Label>();
    }

    @Override
    public void visitClassDef(Tree.ClassDef classDef) {
        for (var f : classDef.fields) {
            f.accept(this);
        }
    }

    @Override
    public void visitMethodDef(Tree.MethodDef funcDefn) {
        if (!funcDefn.isStatic) {
            currentThis = ((Variable) funcDefn.symbol.getAssociatedScope()
                    .lookup("this")).getTemp();
        }
        tr.beginFunc(funcDefn.symbol);
        funcDefn.body.accept(this);
        tr.endFunc();
        currentThis = null;
    }

    @Override
    public void visitTopLevel(Tree.TopLevel program) {
        for (Tree.ClassDef cd : program.classes) {
            cd.accept(this);
        }
    }

    @Override
    public void visitVarDef(Tree.VarDef varDef) {
        if (varDef.symbol.isLocalVar()) {
            Temp t = Temp.createTempI4();
            t.sym = varDef.symbol;
            varDef.symbol.setTemp(t);
        }
    }

    @Override
    public void visitBinary(Tree.Binary expr) {
        expr.lhs.accept(this);
        expr.rhs.accept(this);
        switch (expr.op) {
            case ADD:
                expr.val = tr.genAdd(expr.lhs.val, expr.rhs.val);
                break;
            case SUB:
                expr.val = tr.genSub(expr.lhs.val, expr.rhs.val);
                break;
            case MUL:
                expr.val = tr.genMul(expr.lhs.val, expr.rhs.val);
                break;
            case DIV:
                expr.val = tr.genDiv(expr.lhs.val, expr.rhs.val);
                break;
            case MOD:
                expr.val = tr.genMod(expr.lhs.val, expr.rhs.val);
                break;
            case AND:
                expr.val = tr.genLAnd(expr.lhs.val, expr.rhs.val);
                break;
            case OR:
                expr.val = tr.genLOr(expr.lhs.val, expr.rhs.val);
                break;
            case LT:
                expr.val = tr.genLes(expr.lhs.val, expr.rhs.val);
                break;
            case LE:
                expr.val = tr.genLeq(expr.lhs.val, expr.rhs.val);
                break;
            case GT:
                expr.val = tr.genGtr(expr.lhs.val, expr.rhs.val);
                break;
            case GE:
                expr.val = tr.genGeq(expr.lhs.val, expr.rhs.val);
                break;
            case EQ:
            case NE:
                genEquNeq(expr);
                break;
        }
    }

    private void genEquNeq(Tree.Binary expr) {
        if (expr.lhs.type.equal(BaseType.STRING)
                || expr.rhs.type.equal(BaseType.STRING)) {
            tr.genParm(expr.lhs.val);
            tr.genParm(expr.rhs.val);
            expr.val = tr.genDirectCall(Intrinsic.STRING_EQUAL.label,
                    BaseType.BOOL);
            if (expr.op == Tree.BinaryOp.NE) {
                expr.val = tr.genLNot(expr.val);
            }
        } else {
            if (expr.op == Tree.BinaryOp.EQ)
                expr.val = tr.genEqu(expr.lhs.val, expr.rhs.val);
            else
                expr.val = tr.genNeq(expr.lhs.val, expr.rhs.val);
        }
    }

    @Override
    public void visitAssign(Tree.Assign assign) {
        assign.lhs.accept(this);
        assign.rhs.accept(this);
        switch (assign.lhs.lvKind) {
            case ARRAY_ELEMENT:
                Tree.IndexSel arrayRef = (Tree.IndexSel) assign.lhs;
                Temp esz = tr.genLoadImm4(OffsetCounter.WORD_SIZE);
                Temp t = tr.genMul(arrayRef.index.val, esz);
                Temp base = tr.genAdd(arrayRef.array.val, t);
                tr.genStore(assign.rhs.val, base, 0);
                break;
            case MEMBER_VAR:
                Tree.VarSel varRef = (Tree.VarSel) assign.lhs;
                tr.genStore(assign.rhs.val, varRef.receiver.get().val, varRef.symbol
                        .getOffset());
                break;
            case PARAM_VAR:
            case LOCAL_VAR:
                tr.genAssign(((Tree.VarSel) assign.lhs).symbol.getTemp(),
                        assign.rhs.val);
                break;
        }
    }

    @Override
    public void visitIntLit(Tree.IntLit that) {
        that.val = tr.genLoadImm4(that.value);
    }

    @Override
    public void visitBoolLit(Tree.BoolLit that) {
        that.val = tr.genLoadImm4(that.value ? 1 : 0);
    }

    @Override
    public void visitStringLit(Tree.StringLit that) {
        that.val = tr.genLoadStrConst(that.value);
    }

    @Override
    public void visitNullLit(Tree.NullLit that) {
        that.val = tr.genLoadImm4(0);
    }

    @Override
    public void visitExprEval(Tree.ExprEval exec) {
        exec.expr.accept(this);
    }

    @Override
    public void visitUnary(Tree.Unary expr) {
        expr.operand.accept(this);
        switch (expr.op) {
            case NEG:
                expr.val = tr.genNeg(expr.operand.val);
                break;
            default:
                expr.val = tr.genLNot(expr.operand.val);
        }
    }

    @Override
    public void visitBlock(Tree.Block block) {
        for (var s : block.block) {
            s.accept(this);
        }
    }

    @Override
    public void visitThis(Tree.This thisExpr) {
        thisExpr.val = currentThis;
    }

    @Override
    public void visitReadInt(Tree.ReadInt readInt) {
        readInt.val = tr.genIntrinsicCall(Intrinsic.READ_INT);
    }

    @Override
    public void visitReadLine(Tree.ReadLine readStringExpr) {
        readStringExpr.val = tr.genIntrinsicCall(Intrinsic.READ_LINE);
    }

    @Override
    public void visitReturn(Tree.Return returnStmt) {
        if (returnStmt.expr.isPresent()) {
            returnStmt.expr.get().accept(this);
            tr.genReturn(returnStmt.expr.get().val);
        } else {
            tr.genReturn(null);
        }

    }

    @Override
    public void visitPrint(Tree.Print printStmt) {
        for (Tree.Expr r : printStmt.exprs) {
            r.accept(this);
            tr.genParm(r.val);
            if (r.type.equal(BaseType.BOOL)) {
                tr.genIntrinsicCall(Intrinsic.PRINT_BOOL);
            } else if (r.type.equal(BaseType.INT)) {
                tr.genIntrinsicCall(Intrinsic.PRINT_INT);
            } else if (r.type.equal(BaseType.STRING)) {
                tr.genIntrinsicCall(Intrinsic.PRINT_STRING);
            }
        }
    }

    @Override
    public void visitIndexSel(Tree.IndexSel indexed) {
        indexed.array.accept(this);
        indexed.index.accept(this);
        tr.genCheckArrayIndex(indexed.array.val, indexed.index.val);

        Temp esz = tr.genLoadImm4(OffsetCounter.WORD_SIZE);
        Temp t = tr.genMul(indexed.index.val, esz);
        Temp base = tr.genAdd(indexed.array.val, t);
        indexed.val = tr.genLoad(base, 0);
    }

    @Override
    public void visitVarSel(Tree.VarSel varSel) {
        if (varSel.lvKind == Tree.LValue.LVKind.MEMBER_VAR) {
            varSel.receiver.get().accept(this);
        }

        switch (varSel.lvKind) {
            case MEMBER_VAR:
                varSel.val = tr.genLoad(varSel.receiver.get().val, varSel.symbol.getOffset());
                break;
            default:
                varSel.val = varSel.symbol.getTemp();
                break;
        }
    }

    @Override
    public void visitBreak(Tree.Break breakStmt) {
        tr.genBranch(loopExits.peek());
    }

    @Override
    public void visitCall(Tree.Call callExpr) {
        if (callExpr.isArrayLength) {
            callExpr.receiver.get().accept(this);
            callExpr.val = tr.genLoad(callExpr.receiver.get().val,
                    -OffsetCounter.WORD_SIZE);
        } else {
            callExpr.receiver.ifPresent(expr -> expr.accept(this));
            for (Tree.Expr expr : callExpr.args) {
                expr.accept(this);
            }
            callExpr.receiver.ifPresent(expr -> tr.genParm(expr.val));
            for (Tree.Expr expr : callExpr.args) {
                tr.genParm(expr.val);
            }
            if (callExpr.receiver.isEmpty()) {
                callExpr.val = tr.genDirectCall(
                        callExpr.symbol.getFuncty().label, callExpr.symbol
                                .getReturnType());
            } else {
                Temp vt = tr.genLoad(callExpr.receiver.get().val, 0);
                Temp func = tr.genLoad(vt, callExpr.symbol.getOffset());
                callExpr.val = tr.genIndirectCall(func, callExpr.symbol
                        .getReturnType());
            }
        }

    }

    @Override
    public void visitFor(Tree.For forLoop) {
        forLoop.init.accept(this);
        Label cond = Label.createLabel();
        Label loop = Label.createLabel();
        tr.genBranch(cond);
        tr.genMark(loop);
        forLoop.update.accept(this);
        tr.genMark(cond);
        forLoop.cond.accept(this);
        Label exit = Label.createLabel();
        tr.genBeqz(forLoop.cond.val, exit);
        loopExits.push(exit);
        forLoop.body.accept(this);
        tr.genBranch(loop);
        loopExits.pop();
        tr.genMark(exit);
    }

    @Override
    public void visitIf(Tree.If ifStmt) {
        ifStmt.cond.accept(this);
        if (ifStmt.falseBranch.isPresent()) {
            Label falseLabel = Label.createLabel();
            tr.genBeqz(ifStmt.cond.val, falseLabel);
            ifStmt.trueBranch.accept(this);
            Label exit = Label.createLabel();
            tr.genBranch(exit);
            tr.genMark(falseLabel);
            ifStmt.falseBranch.get().accept(this);
            tr.genMark(exit);
        } else if (ifStmt.trueBranch != null) {
            Label exit = Label.createLabel();
            tr.genBeqz(ifStmt.cond.val, exit);
            if (ifStmt.trueBranch != null) {
                ifStmt.trueBranch.accept(this);
            }
            tr.genMark(exit);
        }
    }

    @Override
    public void visitNewArray(Tree.NewArray newArray) {
        newArray.length.accept(this);
        newArray.val = tr.genNewArray(newArray.length.val);
    }

    @Override
    public void visitNewClass(Tree.NewClass newClass) {
        newClass.val = tr.genDirectCall(newClass.symbol.getNewFuncLabel(),
                BaseType.INT);
    }

    @Override
    public void visitWhile(Tree.While aWhile) {
        Label loop = Label.createLabel();
        tr.genMark(loop);
        aWhile.cond.accept(this);
        Label exit = Label.createLabel();
        tr.genBeqz(aWhile.cond.val, exit);
        loopExits.push(exit);
        if (aWhile.body != null) {
            aWhile.body.accept(this);
        }
        tr.genBranch(loop);
        loopExits.pop();
        tr.genMark(exit);
    }

    @Override
    public void visitClassTest(Tree.ClassTest classTest) {
        classTest.obj.accept(this);
        classTest.val = tr.genInstanceof(classTest.obj.val,
                classTest.symbol);
    }

    @Override
    public void visitClassCast(Tree.ClassCast classCast) {
        classCast.obj.accept(this);
        if (!classCast.obj.type.compatible(classCast.symbol.getType())) {
            tr.genClassCast(classCast.obj.val, classCast.symbol);
        }
        classCast.val = classCast.obj.val;
    }
}
