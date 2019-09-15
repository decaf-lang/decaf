package decaf.frontend.scope;

import decaf.frontend.symbol.ClassSymbol;
import decaf.frontend.symbol.MethodSymbol;
import decaf.frontend.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<Symbol> lookup(String key) {
        var scope = this;
        while (true) {
            var symbol = scope.find(key);
            if (symbol.isPresent()) {
                return symbol;
            }

            if (scope.parentScope.isPresent()) {
                scope = scope.parentScope.get();
            } else {
                break;
            }
        }

        return Optional.empty();
    }

    public List<FormalScope> nestedFormalScopes() {
        var scopes = new ArrayList<FormalScope>();
        for (var symbol : this) {
            if (symbol.isMethodSymbol()) {
                scopes.add(((MethodSymbol) symbol).scope);
            }
        }
        return scopes;
    }

    private ClassSymbol _owner;
}
