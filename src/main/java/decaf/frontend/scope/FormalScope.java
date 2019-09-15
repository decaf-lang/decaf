package decaf.frontend.scope;

import decaf.frontend.symbol.MethodSymbol;

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

    public LocalScope nestedLocalScope() {
        return _nested;
    }

    void setNested(LocalScope scope) {
        _nested = scope;
    }

    private MethodSymbol _owner;

    private LocalScope _nested;
}
