package decaf.frontend.scope;

import decaf.frontend.symbol.Symbol;

import java.util.*;

/**
 * Scopes.
 * <p>
 * A scope stores the mapping from names to {@link Symbol}s. Four kinds of scopes are used:
 * <ul>
 *     <li>global scope: stores globally-defined classes</li>
 *     <li>class scope: stores class members</li>
 *     <li>formal scope: stores parameters</li>
 *     <li>local scope: stores locally-defined variables</li>
 * </ul>
 *
 * @see GlobalScope
 * @see ClassScope
 * @see FormalScope
 * @see LocalScope
 */
public abstract class Scope implements Iterable<Symbol> {

    public enum Kind {
        GLOBAL, CLASS, FORMAL, LOCAL
    }

    public final Kind kind;

    public Scope(Kind kind) {
        this.kind = kind;
    }

    /**
     * Does this scope contains a symbol named {@code key}?
     *
     * @param key symbol's name
     * @return is found or not
     */
    public boolean containsKey(String key) {
        return symbols.containsKey(key);
    }

    /**
     * Get a symbol by name from this scope.
     *
     * @param key symbol's name
     * @return a symbol (if found) or null (if not found)
     */
    public Symbol get(String key) {
        return symbols.get(key);
    }

    /**
     * Find a symbol by name from this scope.
     *
     * @param key symbol's name
     * @return a symbol (if found)
     */
    public Optional<Symbol> find(String key) {
        return Optional.ofNullable(symbols.get(key));
    }

    /**
     * Declare a symbol in this scope.
     *
     * @param symbol symbol
     */
    public void declare(Symbol symbol) {
        symbols.put(symbol.name, symbol);
        symbol.setDomain(this);
    }

    @Override
    public Iterator<Symbol> iterator() {
        var list = new ArrayList<>(symbols.values());
        Collections.sort(list);
        return list.iterator();
    }

    public boolean isEmpty() {
        return symbols.isEmpty();
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

    protected Map<String, Symbol> symbols = new TreeMap<>();
}
