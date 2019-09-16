package decaf.frontend.type;

/**
 * Array type.
 * <p>
 * Note: Decaf arrays are <em>invariant</em>, not <em>covariant</em> as Java arrays do.
 * In Java, if {@code t} {@literal <:} {@code s}, then {@code t[]} {@literal <:} {@code s[]}.
 * But in Decaf, {@code t[]} {@literal <:} {@code s[]} if and only if {@code t} and {@code s} are equal.
 */
public final class ArrayType extends Type {

    public final Type elementType;

    public ArrayType(Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public boolean subtypeOf(Type that) {
        if (that.eq(BuiltInType.ERROR)) {
            return true;
        }
        return eq(that);
    }

    @Override
    public boolean eq(Type that) {
        if (!that.isArrayType()) {
            return false;
        }
        return elementType.eq(((ArrayType) that).elementType);
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
