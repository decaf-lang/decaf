package decaf.scope;

import decaf.printing.IndentPrinter;
import decaf.symbol.ClassSymbol;
import decaf.symbol.Symbol;

public class GlobalScope extends Scope {

    public GlobalScope() {
        super(Kind.GLOBAL);
    }

    @Override
    public boolean isGlobalScope() {
        return true;
    }

    public ClassSymbol getClass(String symbol) {
        return (ClassSymbol) get(symbol);
    }

    @Override
    public void printTo(IndentPrinter pw) {
        pw.println("GLOBAL SCOPE:");
        pw.incIndent();
        for (Symbol symbol : symbols.values()) {
            pw.println(symbol);
        }
        for (Symbol symbol : symbols.values()) {
            ((ClassSymbol) symbol).scope.printTo(pw);
        }
        pw.decIndent();
    }

}
