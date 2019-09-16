package decaf.frontend.type;

/**
 * Built-in types: int, bool, string, void and error (ONLY for type checking).
 */
public final class BuiltInType extends Type {

    private final String name;

    private BuiltInType(String name) {
        this.name = name;
    }

    /**
     * Type {@code int}.
     */
    public static final BuiltInType INT = new BuiltInType("int");

    /**
     * Type {@code bool}.
     */
    public static final BuiltInType BOOL = new BuiltInType("bool");

    /**
     * Type {@code null}, ONLY null values have this type.
     */
    public static final BuiltInType NULL = new BuiltInType("null");

    /**
     * Type {@code string}.
     */
    public static final BuiltInType STRING = new BuiltInType("string");

    /**
     * Type {@code void}, return type ONLY.
     */
    public static final BuiltInType VOID = new BuiltInType("void");

    /**
     * Ill-typed, reserved for type checking. A well-typed program can never contain this.
     */
    public static final BuiltInType ERROR = new BuiltInType("Error");

    @Override
    public boolean subtypeOf(Type that) {
        if (eq(ERROR) || that.eq(ERROR)) {
            return true;
        }
        if (eq(NULL) && that.isClassType()) {
            return true;
        }
        return eq(that);
    }

    @Override
    public boolean eq(Type that) {
        return this == that;
    }

    /**
     * Is this type int, bool, or string?
     *
     * @return checking result
     */
    @Override
    public boolean isBaseType() {
        return eq(INT) || eq(BOOL) || eq(STRING);
    }

    @Override
    public boolean isVoidType() {
        return eq(VOID);
    }

    @Override
    public boolean noError() {
        return !eq(ERROR);
    }

    @Override
    public String toString() {
        return name;
    }

}
