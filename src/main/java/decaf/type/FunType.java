package decaf.type;

import java.util.Iterator;
import java.util.List;

public class FunType extends Type {

    public final Type returnType;

    public final List<Type> argTypes;

    public FunType(Type returnType, List<Type> argTypes) {
        this.returnType = returnType;
        this.argTypes = argTypes;
    }

    public int arity() {
        return argTypes.size();
    }

    @Override
    public boolean subtypeOf(Type type) {
        if (type.eq(BuiltInType.ERROR)) {
            return true;
        }
        if (!type.isFuncType()) {
            return false;
        }
        FunType ft = (FunType) type;
        if (!returnType.subtypeOf(ft.returnType) || argTypes.size() != ft.argTypes.size()) {
            return false;
        }
        Iterator<Type> iter1 = argTypes.iterator();
        iter1.next();
        Iterator<Type> iter2 = ft.argTypes.iterator();
        iter2.next();
        while (iter1.hasNext()) {
            if (!iter2.next().subtypeOf(iter1.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean eq(Type type) {
        if (!type.isFuncType()) return false;
        var that = (FunType) type;
        if (!returnType.eq(that.returnType) || arity() != that.arity()) return false;
        Iterator<Type> iter1 = argTypes.iterator();
        iter1.next();
        Iterator<Type> iter2 = that.argTypes.iterator();
        iter2.next();
        while (iter1.hasNext()) {
            if (!iter2.next().eq(iter1.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Type type : argTypes) {
            sb.append(type + "->");
        }
        sb.append(returnType);
        return sb.toString();
    }

    @Override
    public boolean isFuncType() {
        return true;
    }
}
