package decaf.scope;

import decaf.tree.Tree.Block;
import decaf.symbol.Symbol;
import decaf.printing.IndentPrinter;

public class LocalScope extends Scope {

	private Block node;

	public LocalScope(Block node) {
		this.node = node;
	}

	@Override
	public Kind getKind() {
		return Kind.LOCAL;
	}

	@Override
	public void printTo(IndentPrinter pw) {
		pw.println("LOCAL SCOPE:");
		pw.incIndent();
		for (Symbol symbol : symbols.values()) {
			pw.println(symbol);
		}

		for (var s : node.block) {
			if (s instanceof Block) {
				((Block) s).associatedScope.printTo(pw);
			}
		}
		pw.decIndent();
	}

	@Override
	public boolean isLocalScope() {
		return true;
	}
}
