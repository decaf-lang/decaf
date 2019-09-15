package decaf.frontend.tree;

/**
 * A generic visitor class for trees, with an extra context parameter of type `C`.
 */
public interface Visitor<C> {
    default void visitTopLevel(Tree.TopLevel that, C ctx) {
    }

    default void visitClassDef(Tree.ClassDef that, C ctx) {
    }

    default void visitVarDef(Tree.VarDef that, C ctx) {
    }

    default void visitMethodDef(Tree.MethodDef that, C ctx) {
    }

    default void visitTInt(Tree.TInt that, C ctx) {
    }

    default void visitTBool(Tree.TBool that, C ctx) {
    }

    default void visitTString(Tree.TString that, C ctx) {
    }

    default void visitTVoid(Tree.TVoid that, C ctx) {
    }

    default void visitTClass(Tree.TClass that, C ctx) {
    }

    default void visitTArray(Tree.TArray that, C ctx) {
    }

    default void visitLocalVarDef(Tree.LocalVarDef that, C ctx) {
    }

    default void visitBlock(Tree.Block that, C ctx) {
    }

    default void visitAssign(Tree.Assign that, C ctx) {
    }

    default void visitExprEval(Tree.ExprEval that, C ctx) {
    }

    default void visitSkip(Tree.Skip that, C ctx) {
    }

    default void visitIf(Tree.If that, C ctx) {
    }

    default void visitWhile(Tree.While that, C ctx) {
    }

    default void visitFor(Tree.For that, C ctx) {
    }


    default void visitBreak(Tree.Break that, C ctx) {
    }

    default void visitReturn(Tree.Return that, C ctx) {
    }

    default void visitPrint(Tree.Print that, C ctx) {
    }

    default void visitIntLit(Tree.IntLit that, C ctx) {
    }

    default void visitBoolLit(Tree.BoolLit that, C ctx) {
    }

    default void visitStringLit(Tree.StringLit that, C ctx) {
    }

    default void visitNullLit(Tree.NullLit that, C ctx) {
    }

    default void visitVarSel(Tree.VarSel that, C ctx) {
    }

    default void visitIndexSel(Tree.IndexSel that, C ctx) {
    }

    default void visitCall(Tree.Call that, C ctx) {
    }

    default void visitThis(Tree.This that, C ctx) {
    }

    default void visitUnary(Tree.Unary that, C ctx) {
    }

    default void visitBinary(Tree.Binary that, C ctx) {
    }

    default void visitReadInt(Tree.ReadInt that, C ctx) {
    }

    default void visitReadLine(Tree.ReadLine that, C ctx) {
    }

    default void visitNewClass(Tree.NewClass that, C ctx) {
    }

    default void visitNewArray(Tree.NewArray that, C ctx) {
    }

    default void visitClassTest(Tree.ClassTest that, C ctx) {
    }

    default void visitClassCast(Tree.ClassCast that, C ctx) {
    }

    default void visitId(Tree.Id that, C ctx) {
    }

    default void visitModifiers(Tree.Modifiers that, C ctx) {
    }
}
