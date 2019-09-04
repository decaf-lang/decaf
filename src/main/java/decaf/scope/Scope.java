package decaf.scope;

import decaf.printing.IndentPrinter;
import decaf.symbol.Symbol;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Scope implements Iterable<Symbol> {

	public enum Kind {
        GLOBAL, CLASS, FORMAL, LOCAL
    }

    public final Kind kind;

    public Scope(Kind kind) {
        this.kind = kind;
    }

    public boolean containsKey(String key) {
        return symbols.containsKey(key);
    }

    public Symbol get(String key) {
        return symbols.get(key);
    }

    public Optional<Symbol> find(String key) {
        return Optional.ofNullable(symbols.get(key));
    }

    public void declare(Symbol symbol) {
        symbols.put(symbol.name, symbol);
        symbol.setScope(this);
    }

	@Override
	public Iterator<Symbol> iterator() {
		return symbols.values().iterator();
	}

	public abstract void printTo(IndentPrinter pw); // TODO move to pretty print

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

    protected Map<String, Symbol> symbols = new LinkedHashMap<String, Symbol>();
}
