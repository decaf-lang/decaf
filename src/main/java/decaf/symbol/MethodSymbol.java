package decaf.symbol;

import decaf.scope.ClassScope;
import decaf.scope.FormalScope;
import decaf.tac.Functy;
import decaf.tree.Pos;
import decaf.tree.Tree;
import decaf.type.FunType;
import decaf.type.Type;

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
    private Functy functy;

    private boolean isMain;

    private int offset;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Functy getFuncty() {
        return functy;
    }

    public void setFuncty(Functy functy) {
        this.functy = functy;
    }
}
