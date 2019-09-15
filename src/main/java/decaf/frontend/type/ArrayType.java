package decaf.frontend.type;

public class ArrayType extends Type {

    public final Type elementType;

    public ArrayType(Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public boolean subtypeOf(Type type) {
        if (type.eq(BuiltInType.ERROR)) {
            return true;
        }
        // NOTE: arrays in decaf are _invariant_, but not _covariant_ as Java arrays do.
        // In Java, if t <: s, then t[] <: s[].
        // But in decaf, NO! t[] <: s[] if and only if t == s.
        return eq(type);
    }

    @Override
    public boolean eq(Type type) {
        if (!type.isArrayType()) {
            return false;
        }
        return elementType.eq(((ArrayType) type).elementType);
    }

    @Override
    public String toString() {
        if (elementType.isFuncType()) {
            // NOTE: [] has higher priority than functions, so we must add extra parenthesis, e.g.
            // `(int => int)[]` means an array of functions from integers to integers, but
            // `int => int[]` means a function from integers to integer arrays
            return "(" + elementType + ")[]";
        }
        return elementType + "[]";
    }

    @Override
    public boolean isArrayType() {
        return true;
    }
}
