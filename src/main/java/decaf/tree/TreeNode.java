package decaf.tree;

import java.util.Iterator;

public abstract class TreeNode implements Iterable<Object> {
    public final Tree.Kind kind;

    public final String displayName;

    public final Pos pos;

    public abstract Object treeElementAt(int index);

    public abstract int treeArity();

    public abstract <C> void accept(Visitor<C> v, C ctx);

    public TreeNode(Tree.Kind kind, String displayName, Pos pos) {
        this.kind = kind;
        this.displayName = displayName;
        this.pos = pos;
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < treeArity();
            }

            @Override
            public Object next() {
                var obj = treeElementAt(index);
                index++;
                return obj;
            }
        };
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(displayName);
        sb.append('(');
        var iter = iterator();
        while (iter.hasNext()) {
            sb.append(iter.next());
            if (iter.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }
}
