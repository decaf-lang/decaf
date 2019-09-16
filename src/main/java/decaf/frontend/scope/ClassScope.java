package decaf.frontend.scope;

import decaf.frontend.symbol.ClassSymbol;
import decaf.frontend.symbol.MethodSymbol;
import decaf.frontend.symbol.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class scope: stores class member symbols, i.e. variables and functions. It is owned by a class symbol.
 */
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
        return owner;
    }

    public void setOwner(ClassSymbol owner) {
        this.owner = owner;
    }

    @Override
    public boolean isClassScope() {
        return true;
    }

    /**
     * Lookup a symbol in the entire class scope -- including this class scope and all parents' scopes.
     *
     * @param key symbol's name
     * @return symbol (if found)
     */
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

    /**
     * Collect all formal scopes of the defined function symbols.
     *
     * @return formal scopes
     */
    public List<FormalScope> nestedFormalScopes() {
        var scopes = new ArrayList<FormalScope>();
        for (var symbol : this) {
            if (symbol.isMethodSymbol()) {
                scopes.add(((MethodSymbol) symbol).scope);
            }
        }
        return scopes;
    }

    private ClassSymbol owner;
}
