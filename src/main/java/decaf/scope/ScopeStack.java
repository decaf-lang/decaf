package decaf.scope;

import decaf.symbol.ClassSymbol;
import decaf.symbol.MethodSymbol;
import decaf.symbol.Symbol;
import decaf.tree.Pos;

import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;

public class ScopeStack {

    public final GlobalScope global;

    public ScopeStack(GlobalScope global) {
        this.global = global;
    }

    /**
     * Open a scope.
     * <p>
     * If the current scope is a class scope, then we must first push all its super classes and then push this.
     * Otherwise, only push the `scope`.
     * <p>
     * REQUIRES: you don't open multiple class scopes, and never open a class scope when the current scope is
     * a formal/local scope.
     */
    public void open(Scope scope) {
        assert !scope.isGlobalScope();
        if (scope.isClassScope()) {
            assert !currentScope().isFormalOrLocalScope();
            var classScope = (ClassScope) scope;
            classScope.parentScope.ifPresent(this::open);
            _current_class = classScope.getOwner();
        } else if (scope.isFormalScope()) {
            var formalScope = (FormalScope) scope;
            _current_method = formalScope.getOwner();
        }
        scopeStack.push(scope);
    }

    /**
     * Close the current scope.
     * <p>
     * If the current scope is a class scope, then we must close this class and all super classes.
     * Since the global scope is never pushed to `scopeStack`, we need to pop all scopes!
     * Otherwise, only pop the current scope.
     */
    public void close() {
        assert !scopeStack.isEmpty();
        Scope scope = scopeStack.pop();
        if (scope.isClassScope()) {
            while (!scopeStack.isEmpty()) {
                scopeStack.pop();
            }
        }
    }

    public Scope currentScope() {
        if (scopeStack.isEmpty()) return global;
        return scopeStack.peek();
    }

    public ClassSymbol currentClass() {
        Objects.requireNonNull(_current_class);
        return _current_class;
    }

    public MethodSymbol currentMethod() {
        Objects.requireNonNull(_current_method);
        return _current_method;
    }

    /**
     * Lookup a symbol by key. By saying "lookup", the user expects that the symbol is found.
     * In this way, we will always search in all possible scopes and returns the innermost result.
     *
     * @param key the key
     * @return innermost found symbol (if any)
     */
    public Optional<Symbol> lookup(String key) {
        return findWhile(key, whatever -> true, whatever -> true);
    }

    public Optional<Symbol> lookupBefore(String key, Pos pos) {
        return findWhile(key, whatever -> true, s -> !(s.domain().isLocalScope() && s.pos.compareTo(pos) > 0));
    }

    /**
     * Find if `key` conflicts with some already defined symbol. Rules:
     * - if the current scope is local scope or formal scope, `key` cannot conflict with any already defined symbol
     * up till the formal scope, and it cannot conflict with any names in the global scope
     * - if the current scope is class scope or global scope, `key` cannot conflict with any already defined symbol
     * <p>
     * NO override checking is performed here, the type checker should tell if the returned conflicting symbol is
     * in fact allowed or not.
     *
     * @param key the key
     * @return innermost conflicting symbol (if any)
     */
    public Optional<Symbol> findConflict(String key) {
        if (currentScope().isFormalOrLocalScope())
            return findWhile(key, Scope::isFormalOrLocalScope, whatever -> true).or(() -> global.find(key));
        return lookup(key);
    }

    public boolean containsClass(String key) {
        return global.containsKey(key);
    }

    public Optional<ClassSymbol> lookupClass(String key) {
        return Optional.ofNullable(global.getClass(key));
    }

    public ClassSymbol getClass(String key) {
        return global.getClass(key);
    }

    public void declare(Symbol symbol) {
        currentScope().declare(symbol);
    }

    private Stack<Scope> scopeStack = new Stack<Scope>();

    private ClassSymbol _current_class;
    private MethodSymbol _current_method;

    private Optional<Symbol> findWhile(String key, Predicate<Scope> cond, Predicate<Symbol> validator) {
        ListIterator<Scope> iter = scopeStack.listIterator(scopeStack.size());
        while (iter.hasPrevious()) {
            var scope = iter.previous();
            if (!cond.test(scope)) return Optional.empty();
            var symbol = scope.find(key);
            if (symbol.isPresent() && validator.test(symbol.get())) return symbol;
        }
        return cond.test(global) ? global.find(key) : Optional.empty();
    }
}
