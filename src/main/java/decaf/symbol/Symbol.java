package decaf.symbol;

import java.util.Comparator;

import decaf.tree.Pos;
import decaf.scope.Scope;
import decaf.type.Type;

public abstract class Symbol {
    public final String name;

    public final Type type;

    public final Pos pos;

    protected Symbol(String name, Type type, Pos pos) {
        this.name = name;
        this.type = type;
        this.pos = pos;
    }

    public Scope getScope() {
        return _definedIn;
    }

    public void setScope(Scope definedIn) {
        this._definedIn = definedIn;
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

    protected abstract String nameStr();

    @Override
    public String toString() {
        return pos + " -> " + nameStr() + " : " + type;
    }

    protected Scope _definedIn;

    // TODO: remove

    protected int order;

    public static final Comparator<Symbol> LOCATION_COMPARATOR = new Comparator<Symbol>() {

        @Override
        public int compare(Symbol o1, Symbol o2) {
            return o1.pos.compareTo(o2.pos);
        }

    };

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
