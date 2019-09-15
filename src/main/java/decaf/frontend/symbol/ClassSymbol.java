package decaf.frontend.symbol;

import decaf.lowlevel.Label;
import decaf.lowlevel.tac.ClassInfo;
import decaf.lowlevel.tac.TAC;
import decaf.frontend.scope.ClassScope;
import decaf.frontend.scope.GlobalScope;
import decaf.frontend.tree.Pos;
import decaf.frontend.type.ClassType;

import java.util.HashSet;
import java.util.Optional;

public class ClassSymbol extends Symbol {

    public final Optional<ClassSymbol> parentSymbol;

    public final ClassType type;

    public final ClassScope scope;

    public ClassSymbol(String name, ClassType type, ClassScope scope, Pos pos) {
        super(name, type, pos);
        this.parentSymbol = Optional.empty();
        this.scope = scope;
        this.type = type;
        scope.setOwner(this);
    }

    public ClassSymbol(String name, ClassSymbol parentSymbol, ClassType type, ClassScope scope, Pos pos) {
        super(name, type, pos);
        this.parentSymbol = Optional.of(parentSymbol);
        this.scope = scope;
        this.type = type;
        scope.setOwner(this);
    }

    @Override
    public GlobalScope domain() {
        return (GlobalScope) _definedIn;
    }

    @Override
    public boolean isClassSymbol() {
        return true;
    }

    public void setMainClass() {
        _main = true;
    }

    public boolean isMainClass() {
        return _main;
    }

    @Override
    protected String str() {
        return "class " + name + parentSymbol.map(classSymbol -> " : " + classSymbol.name).orElse("");
    }

    // For tac generation.
    public ClassInfo getInfo() {
        var memberVariables = new HashSet<String>();
        var memberMethods = new HashSet<String>();
        var staticMethods = new HashSet<String>();

        for (var symbol : scope) {
            if (symbol.isVarSymbol()) {
                memberVariables.add(symbol.name);
            } else if (symbol.isMethodSymbol()) {
                var methodSymbol = (MethodSymbol) symbol;
                if (methodSymbol.isStatic()) {
                    staticMethods.add(methodSymbol.name);
                } else {
                    memberMethods.add(methodSymbol.name);
                }
            }
        }

        return new ClassInfo(name, parentSymbol.map(symbol -> symbol.name), memberVariables, memberMethods,
                staticMethods, isMainClass());
    }

    private boolean _main;

    // TODO: remove

    private int order;

    private boolean check;

    private int numNonStaticFunc;

    private int numVar;

    private int size;

    private TAC.VTable vtable;

    private Label newFuncLabel;

    public Label getNewFuncLabel() {
        return newFuncLabel;
    }

    public void setNewFuncLabel(Label newFuncLabel) {
        this.newFuncLabel = newFuncLabel;
    }

    public TAC.VTable getVtable() {
        return vtable;
    }

    public void setVtable(TAC.VTable vtable) {
        this.vtable = vtable;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumNonStaticFunc() {
        return numNonStaticFunc;
    }

    public void setNumNonStaticFunc(int numNonStaticFunc) {
        this.numNonStaticFunc = numNonStaticFunc;
    }

    public int getNumVar() {
        return numVar;
    }

    public void setNumVar(int numVar) {
        this.numVar = numVar;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
}
