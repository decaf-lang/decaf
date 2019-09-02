package decaf.tree;

public abstract class TreeNode {
    public final Tree.Kind kind;

    public final String displayName;

    public final Pos pos;

    public abstract Object treeElementAt(int index);

    public abstract int treeArity();

    public abstract void accept(Tree.Visitor v);

    public TreeNode(Tree.Kind kind, String displayName, Pos pos) {
        this.kind = kind;
        this.displayName = displayName;
        this.pos = pos;
    }

    public Pos getLocation() {
        return pos;
    }
}
