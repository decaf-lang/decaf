package decaf.type;

public class BuiltInType extends Type {

    private final String name;

    private BuiltInType(String name) {
        this.name = name;
    }

    public static final BuiltInType INT = new BuiltInType("int");

    public static final BuiltInType BOOL = new BuiltInType("bool");

    public static final BuiltInType NULL = new BuiltInType("null");

    public static final BuiltInType ERROR = new BuiltInType("Error");

    public static final BuiltInType STRING = new BuiltInType("string");

    public static final BuiltInType VOID = new BuiltInType("void");

    @Override
    public boolean subtypeOf(Type type) {
        if (eq(ERROR) || type.eq(ERROR)) {
            return true;
        }
        if (eq(NULL) && type.isClassType()) {
            return true;
        }
        return eq(type);
    }

    @Override
    public boolean eq(Type type) {
        return this == type;
    }


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
