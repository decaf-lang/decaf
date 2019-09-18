package decaf.frontend.symbol;

import decaf.frontend.scope.ClassScope;
import decaf.frontend.tree.Pos;
import decaf.frontend.type.ClassType;
import decaf.frontend.type.Type;
import decaf.lowlevel.instr.Temp;

/**
 * Variable symbol, representing a member (defined as a class member), param (defined as a functional parameter),
 * or a local (defined in a local scope) variable definition.
 */
public final class VarSymbol extends Symbol {

    public VarSymbol(String name, Type type, Pos pos) {
        super(name, type, pos);
    }

    /**
     * Create a variable symbol for {@code this}.
     *
     * @param type type of {@code this}
     * @param pos  position of {@code this}
     * @return variable symbol
     */
    public static VarSymbol thisVar(ClassType type, Pos pos) {
        return new VarSymbol("this", type, pos);
    }

    @Override
    public boolean isVarSymbol() {
        return true;
    }

    @Override
    protected String str() {
        return String.format("variable %s%s : %s", isParam() ? "@" : "", name, type);
    }

    public boolean isLocalVar() {
        return definedIn.isLocalScope();
    }

    public boolean isParam() {
        return definedIn.isFormalScope();
    }

    public boolean isMemberVar() {
        return definedIn.isClassScope();
    }

    /**
     * Get the owner of a member variable, which is a class symbol.
     *
     * @return owner
     * @throws IllegalArgumentException if this is not a member variable
     */
    public ClassSymbol getOwner() {
        if (!isMemberVar()) {
            throw new IllegalArgumentException("this var symbol is not a member var");
        }
        return ((ClassScope) definedIn).getOwner();
    }

    /**
     * Temp, reserved for {@link decaf.frontend.tacgen.TacGen}.
     */
    public Temp temp;
}
