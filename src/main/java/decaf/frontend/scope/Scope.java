package decaf.frontend.scope;

import decaf.frontend.symbol.Symbol;

import java.util.*;

public abstract class Scope implements Iterable<Symbol> {

    public enum Kind {
        GLOBAL, CLASS, FORMAL, LOCAL
    }

    public final Kind kind;

    public Scope(Kind kind) {
        this.kind = kind;
    }

    public boolean containsKey(String key) {
        return _symbols.containsKey(key);
    }

    public Symbol get(String key) {
        return _symbols.get(key);
    }

    public Optional<Symbol> find(String key) {
        return Optional.ofNullable(_symbols.get(key));
    }

    public void declare(Symbol symbol) {
        _symbols.put(symbol.name, symbol);
        symbol.setDomain(this);
    }

    @Override
    public Iterator<Symbol> iterator() {
        var list = new ArrayList<>(_symbols.values());
        Collections.sort(list, Symbol.POS_COMPARATOR);
        return list.iterator();
    }

    public boolean isEmpty() {
        return _symbols.isEmpty();
    }

    public boolean isGlobalScope() {
        return false;
    }

    public boolean isClassScope() {
        return false;
    }

    public boolean isLocalScope() {
        return false;
    }

    public boolean isFormalScope() {
        return false;
    }

    public boolean isFormalOrLocalScope() {
        return isFormalScope() || isLocalScope();
    }

    protected Map<String, Symbol> _symbols = new LinkedHashMap<String, Symbol>();
}
