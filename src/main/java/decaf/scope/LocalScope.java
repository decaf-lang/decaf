package decaf.scope;

import decaf.printing.IndentPrinter;
import decaf.symbol.Symbol;
import decaf.tree.Tree.Block;

public class LocalScope extends Scope {

	public LocalScope(Block tree) {
		super(Kind.LOCAL);
		this._tree = tree;
	}

	@Override
	public boolean isLocalScope() {
		return true;
	}

	@Override
	public void printTo(IndentPrinter pw) {
		pw.println("LOCAL SCOPE:");
		pw.incIndent();
		for (Symbol symbol : symbols.values()) {
			pw.println(symbol);
		}

		for (var s : _tree.stmts) {
			if (s instanceof Block) {
				((Block) s).scope.printTo(pw);
			}
		}
		pw.decIndent();
	}

	private Block _tree;
}
