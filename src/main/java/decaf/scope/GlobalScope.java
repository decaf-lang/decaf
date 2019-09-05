package decaf.scope;

import decaf.symbol.ClassSymbol;

import java.util.ArrayList;
import java.util.List;

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
