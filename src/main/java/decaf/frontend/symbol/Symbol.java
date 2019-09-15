package decaf.frontend.symbol;

import java.util.Comparator;

import decaf.frontend.tree.Pos;
import decaf.frontend.scope.Scope;
import decaf.frontend.type.Type;

public abstract class Symbol {
    public final String name;

    public final Type type;

    public final Pos pos;

    protected Symbol(String name, Type type, Pos pos) {
        this.name = name;
        this.type = type;
        this.pos = pos;
    }

    public Scope domain() {
        return _definedIn;
    }

    public void setDomain(Scope scope) {
        this._definedIn = scope;
    }

    public boolean isClassSymbol() {
        return false;
    }

    public boolean isVarSymbol() {
        return false;
    }

    public boolean isMethodSymbol() {
        return false;
    }

    protected abstract String str();

    @Override
    public String toString() {
        return pos + " -> " + str();
    }

    protected Scope _definedIn;

    public static final Comparator<Symbol> POS_COMPARATOR = (o1, o2) -> o1.pos.compareTo(o2.pos);

    // TODO: remove

    protected int order;

    public static final Comparator<Symbol> ORDER_COMPARATOR = new Comparator<Symbol>() {

        @Override
        public int compare(Symbol o1, Symbol o2) {
            return o1.order > o2.order ? 1 : o1.order == o2.order ? 0 : -1;
        }

    };

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
