package decaf.scope;

import java.util.Optional;
import java.util.TreeSet;

import decaf.symbol.ClassSymbol;
import decaf.symbol.MethodSymbol;
import decaf.symbol.Symbol;
import decaf.printing.IndentPrinter;

public class ClassScope extends Scope {

    public final Optional<ClassScope> parentScope;

    public ClassScope() {
        super(Kind.CLASS);
        this.parentScope = Optional.empty();
    }

    public ClassScope(ClassScope superScope) {
        super(Kind.CLASS);
        this.parentScope = Optional.of(superScope);
    }

    public ClassSymbol getOwner() {
        return _owner;
    }

    public void setOwner(ClassSymbol owner) {
        _owner = owner;
    }

    @Override
    public boolean isClassScope() {
        return true;
    }

    @Override
    public void printTo(IndentPrinter pw) {
        TreeSet<Symbol> ss = new TreeSet<Symbol>(Symbol.LOCATION_COMPARATOR);
        for (Symbol symbol : symbols.values()) {
            ss.add(symbol);
        }
        pw.println("CLASS SCOPE OF '" + _owner.name + "':");
        pw.incIndent();
        for (Symbol symbol : ss) {
            pw.println(symbol);
        }
        for (Symbol symbol : ss) {
            if (symbol.isMethodSymbol()) {
                ((MethodSymbol) symbol).scope.printTo(pw);
            }
        }
        pw.decIndent();
    }

    public Optional<Symbol> lookupVisible(String name) {
        for (ClassScope cs = this; cs.parentScope.isPresent(); cs = cs.parentScope.get()) {
            var symbol = cs.find(name);
            if (symbol.isPresent()) {
                return symbol;
            }
        }
        return Optional.empty();
    }

    private ClassSymbol _owner;
}
