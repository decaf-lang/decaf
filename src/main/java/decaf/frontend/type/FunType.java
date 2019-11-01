package decaf.frontend.type;

import java.util.List;

/**
 * Function type.
 */
public final class FunType extends Type {

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

        // Recall: (t1, t2, ..., tn) => t <: (s1, s2, ..., sn) => s if t <: s and si <: ti for every i
        FunType that = (FunType) type;
        if (!this.returnType.subtypeOf(that.returnType) || this.arity() != that.arity()) return false;
        var thisArg = this.argTypes.iterator();
        var thatArg = that.argTypes.iterator();
        while (thisArg.hasNext()) {
            if (!thatArg.next().subtypeOf(thisArg.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean eq(Type type) {
        if (!type.isFuncType()) return false;
        var that = (FunType) type;
        if (!this.returnType.eq(that.returnType) || this.arity() != that.arity()) return false;
        var thisArg = this.argTypes.iterator();
        var thatArg = that.argTypes.iterator();
        while (thisArg.hasNext()) {
            if (!thatArg.next().eq(thisArg.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        if (argTypes.isEmpty()) {
            sb.append("()");
        } else if (argTypes.size() == 1) {
            var arg = argTypes.get(0).toString();
            if (argTypes.get(0).isFuncType()) {
                arg = "(" + arg + ")";
            }
            sb.append(arg);
        } else {
            sb.append('(');
            for (int i = 0; i < argTypes.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(argTypes.get(i));
            }
            sb.append(')');
        }
        sb.append(" => ");
        sb.append(returnType);
        return sb.toString();
    }

    @Override
    public boolean isFuncType() {
        return true;
    }
}
