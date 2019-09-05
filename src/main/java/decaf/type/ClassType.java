package decaf.type;

import java.util.Optional;

public class ClassType extends Type {

    public final String name;

    public final Optional<ClassType> superType;

    public ClassType(String name, ClassType superType) {
        this.name = name;
        this.superType = Optional.of(superType);
    }

    public ClassType(String name) {
        this.name = name;
        this.superType = Optional.empty();
    }

    @Override
    public boolean subtypeOf(Type type) {
        if (type.eq(BuiltInType.ERROR)) {
            return true;
        }

        if (!type.isClassType()) {
            return false;
        }

        var t = this;
        while (true) {
            if (t.eq(type)) return true;
            if (t.superType.isPresent()) t = t.superType.get();
            else break;
        }

        return false;
    }

    @Override
    public boolean eq(Type type) {
        return type.isClassType() && name.equals(((ClassType) type).name);
    }

    @Override
    public boolean isClassType() {
        return true;
    }

    @Override
    public String toString() {
        return "class : " + name;
    }
}
