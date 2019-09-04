package decaf.type;

public abstract class Type {
    public boolean isBaseType() {
        return false;
    }

    public boolean isArrayType() {
        return false;
    }

    public boolean isClassType() {
        return false;
    }

    public boolean isFuncType() {
        return false;
    }

    public boolean isVoidType() {
        return false;
    }

    public boolean noError() {
        return true;
    }

    public boolean hasError() {
        return !noError();
    }

    public abstract boolean subtypeOf(Type type);

    public abstract boolean eq(Type type);

    public abstract String toString();
}
