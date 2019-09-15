package decaf.frontend.symbol;

import decaf.lowlevel.Temp;
import decaf.frontend.scope.ClassScope;
import decaf.frontend.tree.Pos;
import decaf.frontend.type.ClassType;
import decaf.frontend.type.Type;

public class VarSymbol extends Symbol {

    public VarSymbol(String name, Type type, Pos pos) {
        super(name, type, pos);
    }

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
        return _definedIn.isLocalScope();
    }

    public boolean isParam() {
        return _definedIn.isFormalScope();
    }

    public boolean isMemberVar() {
        return _definedIn.isClassScope();
    }

    public ClassSymbol getOwner() {
        assert isMemberVar();
        return ((ClassScope) _definedIn).getOwner();
    }

    // For tac gen
    public Temp temp;
}
