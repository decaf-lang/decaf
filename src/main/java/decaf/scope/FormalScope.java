package decaf.scope;

import decaf.tree.Tree.Block;
import decaf.symbol.MethodSymbol;
import decaf.symbol.Symbol;
import decaf.printing.IndentPrinter;

public class FormalScope extends Scope {

    public FormalScope() {
        super(Kind.FORMAL);
    }

    public MethodSymbol getOwner() {
        return _owner;
    }

    public void setOwner(MethodSymbol owner) {
        _owner = owner;
    }

    @Override
    public boolean isFormalScope() {
        return true;
    }

    @Override
    public void printTo(IndentPrinter pw) {
        pw.println("FORMAL SCOPE OF '" + _owner.name + "':");
        pw.incIndent();
        for (Symbol symbol : symbols.values()) {
            pw.println(symbol);
        }
        _tree.scope.printTo(pw);
        pw.decIndent();
    }

    private MethodSymbol _owner;

    private Block _tree;
}
