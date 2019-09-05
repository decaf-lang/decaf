package decaf.symbol;

import decaf.scope.ClassScope;
import decaf.scope.GlobalScope;
import decaf.tac.Label;
import decaf.tac.VTable;
import decaf.tree.Pos;
import decaf.type.ClassType;

import java.util.Optional;

public class ClassSymbol extends Symbol {

    public final Optional<ClassSymbol> baseSymbol;

    public final ClassType type;

    public final ClassScope scope;

    public ClassSymbol(String name, ClassType type, ClassScope scope, Pos pos) {
        super(name, type, pos);
        this.baseSymbol = Optional.empty();
        this.scope = scope;
        this.type = type;
        scope.setOwner(this);
    }

    public ClassSymbol(String name, ClassSymbol baseSymbol, ClassType type, ClassScope scope, Pos pos) {
        super(name, type, pos);
        this.baseSymbol = Optional.of(baseSymbol);
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

    @Override
    protected String str() {
        return "class " + name + baseSymbol.map(classSymbol -> " : " + classSymbol.name).orElse("");
    }

	// TODO: remove

    private int order;

    private boolean check;

    private int numNonStaticFunc;

    private int numVar;

    private int size;

    private VTable vtable;

    private Label newFuncLabel;

    public Label getNewFuncLabel() {
        return newFuncLabel;
    }

    public void setNewFuncLabel(Label newFuncLabel) {
        this.newFuncLabel = newFuncLabel;
    }

    public VTable getVtable() {
        return vtable;
    }

    public void setVtable(VTable vtable) {
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
