package decaf.frontend.type;

import java.util.Optional;

/**
 * Class type.
 */
public final class ClassType extends Type {

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
    public boolean subtypeOf(Type that) {
        if (that.eq(BuiltInType.ERROR)) {
            return true;
        }

        if (!that.isClassType()) {
            return false;
        }

        var t = this;
        while (true) {
            if (t.eq(that)) return true;
            if (t.superType.isPresent()) t = t.superType.get();
            else break;
        }

        return false;
    }

    @Override
    public boolean eq(Type that) {
        return that.isClassType() && name.equals(((ClassType) that).name);
    }

    @Override
    public boolean isClassType() {
        return true;
    }

    @Override
    public String toString() {
        return "class " + name;
    }
}
