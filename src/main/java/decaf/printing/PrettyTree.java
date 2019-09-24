package decaf.printing;

import decaf.frontend.tree.TreeNode;
import decaf.lowlevel.log.IndentPrinter;

import java.util.List;
import java.util.Optional;

/**
 * Pretty print a tree. PA1 output.
 */
public final class PrettyTree extends PrettyPrinter<TreeNode> {

    public PrettyTree(IndentPrinter printer) {
        super(printer);
    }

    private void prettyElement(Object element) {
        if (element == null) {
            printer.println("<null: here is a bug>");
        } else if (element instanceof TreeNode) {
            pretty((TreeNode) element);
        } else if (element instanceof Optional) {
            var opt = (Optional) element;
            if (opt.isPresent()) {
                prettyElement(opt.get());
            } else {
                printer.println("<none>");
            }
        } else if (element instanceof List) {
            printer.println("List");
            printer.incIndent();
            var list = (List<?>) element;
            if (list.isEmpty()) printer.println("<empty>");
            else list.forEach(this::prettyElement);
            printer.decIndent();
        } else {
            var s = element.toString();
            if (!s.isEmpty()) {
                printer.println(s);
            }
        }
    }

    @Override
    public void pretty(TreeNode node) {
        printer.formatLn("%s @ %s", node.displayName, node.pos);
        printer.incIndent();
        node.forEach(this::prettyElement);
        printer.decIndent();
    }
}
