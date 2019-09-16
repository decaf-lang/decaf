package decaf.frontend.scope;

import decaf.frontend.symbol.ClassSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Global scope: stores globally-defined class symbols.
 */
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

    /**
     * Collect all scopes of the defined class symbols.
     *
     * @return class scopes
     */
    public List<ClassScope> nestedClassScopes() {
        var scopes = new ArrayList<ClassScope>();
        for (var symbol : this) {
            if (symbol.isClassSymbol()) {
                scopes.add(((ClassSymbol) symbol).scope);
            }
        }
        return scopes;
    }
}
