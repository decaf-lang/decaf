package decaf.frontend.symbol;

import decaf.frontend.scope.ClassScope;
import decaf.frontend.scope.FormalScope;
import decaf.frontend.tree.Pos;
import decaf.frontend.tree.Tree;
import decaf.frontend.type.FunType;
import decaf.frontend.type.Type;

public class MethodSymbol extends Symbol {

    public final FormalScope scope;

    public final Tree.Modifiers modifiers;

    public final ClassSymbol owner;

    public MethodSymbol(String name, Type type, FormalScope scope, Pos pos, Tree.Modifiers modifiers,
                        ClassSymbol owner) {
        super(name, type, pos);
        this.scope = scope;
        this.modifiers = modifiers;
        this.owner = owner;
        scope.setOwner(this);
    }

    @Override
    public ClassScope domain() {
        return (ClassScope) _definedIn;
    }

    @Override
    public boolean isMethodSymbol() {
        return true;
    }

    @Override
    protected String str() {
        var modStr = modifiers.toString();
        if (!modStr.isEmpty()) modStr += " ";
        return modStr + String.format("function %s : %s", name, type);
    }

    public FunType getFunType() {
        return (FunType) type;
    }

    public Type getReturnType() {
        return getFunType().returnType;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain() {
        this.isMain = true;
    }

    public boolean isStatic() {
        return modifiers.isStatic();
    }

    // TODO

    private boolean isMain;
}
