package decaf.frontend.tree;

import java.util.Iterator;

/**
 * The base class of all tree nodes defined in class {@link Tree}.
 * <p>
 * Abstract syntax trees are just trees, so they have their children. When manipulating a tree, we usually traverse all
 * child nodes and do recursion. To avoid specifying the traverse method for every kind of tree nodes, here we build
 * a bunch of methods to obtain their children, and an iterator to traverse them all. This makes our life much much
 * easier in implementing pretty print {@link decaf.printing.PrettyTree}. This technique is inspired by Scala's case
 * class, which is missing in Java.
 */
public abstract class TreeNode implements Iterable<Object> {
    /**
     * Which kind of tree node is it?
     */
    public final Tree.Kind kind;

    /**
     * Name.
     */
    public final String displayName;

    /**
     * Position.
     */
    public final Pos pos;

    /**
     * How many children (or here we call element) does it have?
     *
     * @return the number of tree elements
     */
    public abstract int treeArity();

    /**
     * What is the index-th child?
     *
     * @param index index from {@code 0} until {@code treeArity() - 1}
     * @return the child/element
     */
    public abstract Object treeElementAt(int index);

    /**
     * What to do when a visitor is visiting this node?
     *
     * @param v   visitor
     * @param ctx associated context
     * @param <C> type of context
     */
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
