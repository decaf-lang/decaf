package decaf.frontend.scope;

import java.util.ArrayList;
import java.util.List;

public class LocalScope extends Scope {

    public LocalScope(Scope parent) {
        super(Kind.LOCAL);
        assert parent.isFormalOrLocalScope();
        if (parent.isFormalScope()) {
            ((FormalScope) parent).setNested(this);
        } else {
            ((LocalScope) parent)._nested.add(this);
        }
    }

    @Override
    public boolean isLocalScope() {
        return true;
    }

    public List<LocalScope> nestedLocalScopes() {
        return _nested;
    }

    private List<LocalScope> _nested = new ArrayList<>();
}
