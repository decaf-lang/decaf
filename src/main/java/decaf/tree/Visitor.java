package decaf.tree;

/**
 * A generic visitor class for trees.
 */
public interface Visitor {
    default void visitTopLevel(Tree.TopLevel that) {
    }

    default void visitClassDef(Tree.ClassDef that) {
    }

    default void visitVarDef(Tree.VarDef that) {
    }

    default void visitMethodDef(Tree.MethodDef that) {
    }

    default void visitTInt(Tree.TInt that) {
    }

    default void visitTBool(Tree.TBool that) {
    }

    default void visitTString(Tree.TString that) {
    }

    default void visitTVoid(Tree.TVoid that) {
    }

    default void visitTClass(Tree.TClass that) {
    }

    default void visitTArray(Tree.TArray that) {
    }

    default void visitLocalVarDef(Tree.LocalVarDef that) {
    }

    default void visitBlock(Tree.Block that) {
    }

    default void visitAssign(Tree.Assign that) {
    }

    default void visitExprEval(Tree.ExprEval that) {
    }

    default void visitSkip(Tree.Skip that) {
    }

    default void visitIf(Tree.If that) {
    }

    default void visitWhile(Tree.While that) {
    }

    default void visitFor(Tree.For that) {
    }


    default void visitBreak(Tree.Break that) {
    }

    default void visitReturn(Tree.Return that) {
    }

    default void visitPrint(Tree.Print that) {
    }

    default void visitIntLit(Tree.IntLit that) {
    }

    default void visitBoolLit(Tree.BoolLit that) {
    }

    default void visitStringLit(Tree.StringLit that) {
    }

    default void visitNullLit(Tree.NullLit that) {
    }

    default void visitVarSel(Tree.VarSel that) {
    }

    default void visitIndexSel(Tree.IndexSel that) {
    }

    default void visitCall(Tree.Call that) {
    }

    default void visitThis(Tree.This that) {
    }

    default void visitUnary(Tree.Unary that) {
    }

    default void visitBinary(Tree.Binary that) {
    }

    default void visitReadInt(Tree.ReadInt that) {
    }

    default void visitReadLine(Tree.ReadLine that) {
    }

    default void visitNewClass(Tree.NewClass that) {
    }

    default void visitNewArray(Tree.NewArray that) {
    }

    default void visitClassTest(Tree.ClassTest that) {
    }

    default void visitClassCast(Tree.ClassCast that) {
    }

    default void visitId(Tree.Id that) {
    }

    default void visitModifiers(Tree.Modifiers that) {
    }
}
