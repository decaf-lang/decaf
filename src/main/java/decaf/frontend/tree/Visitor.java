package decaf.frontend.tree;

/**
 * A generic visitor class for trees, with an extra context parameter.
 * <p>
 * We also support a special method {@link #visitOthers} so that you can build a default handler for those
 * unspecified trees (whose visitor methods are not overriden). The default default handler does NOTHING.
 *
 * @param <C> type of the context, could be whatever you need
 */
public interface Visitor<C> {

    default void visitTopLevel(Tree.TopLevel that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitClassDef(Tree.ClassDef that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitVarDef(Tree.VarDef that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitMethodDef(Tree.MethodDef that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitTInt(Tree.TInt that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitTBool(Tree.TBool that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitTString(Tree.TString that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitTVoid(Tree.TVoid that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitTClass(Tree.TClass that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitTArray(Tree.TArray that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitLocalVarDef(Tree.LocalVarDef that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitBlock(Tree.Block that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitAssign(Tree.Assign that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitExprEval(Tree.ExprEval that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitSkip(Tree.Skip that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitIf(Tree.If that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitWhile(Tree.While that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitFor(Tree.For that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitBreak(Tree.Break that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitReturn(Tree.Return that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitPrint(Tree.Print that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitIntLit(Tree.IntLit that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitBoolLit(Tree.BoolLit that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitStringLit(Tree.StringLit that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitNullLit(Tree.NullLit that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitVarSel(Tree.VarSel that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitIndexSel(Tree.IndexSel that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitCall(Tree.Call that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitThis(Tree.This that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitUnary(Tree.Unary that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitBinary(Tree.Binary that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitReadInt(Tree.ReadInt that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitReadLine(Tree.ReadLine that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitNewClass(Tree.NewClass that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitNewArray(Tree.NewArray that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitClassTest(Tree.ClassTest that, C ctx) {
        visitOthers(that, ctx);
    }

    default void visitClassCast(Tree.ClassCast that, C ctx) {
        visitOthers(that, ctx);
    }

    /* The default handler */
    default void visitOthers(TreeNode that, C ctx) {
        // do nothing
    }
}
