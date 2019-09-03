package decaf.typecheck;

import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.error.*;
import decaf.scope.ClassScope;
import decaf.scope.GlobalScope;
import decaf.scope.LocalScope;
import decaf.scope.ScopeStack;
import decaf.symbol.Class;
import decaf.symbol.Function;
import decaf.symbol.Symbol;
import decaf.symbol.Variable;
import decaf.tree.Tree;
import decaf.tree.Visitor;
import decaf.type.BaseType;
import decaf.type.FuncType;

import java.util.Iterator;

public class Namer extends Phase<Tree.TopLevel, Tree.TopLevel> implements Visitor {

    public Namer(Config config) {
        super("namer", config);
    }

    @Override
    public Tree.TopLevel transform(Tree.TopLevel tree) {
        tree.table = this.table;
        tree.accept(this);
        return tree;
    }

    private ScopeStack table = new ScopeStack();

    // root
    @Override
    public void visitTopLevel(Tree.TopLevel program) {
        program.globalScope = new GlobalScope();
        table.open(program.globalScope);
        for (Tree.ClassDef cd : program.classes) {
            Class c = new Class(cd.id.name, cd.parent.get().name, cd.getLocation(), null); // FIXME
            Class earlier = table.lookupClass(cd.displayName);
            if (earlier != null) {
                issue(new DeclConflictError(cd.getLocation(), cd.displayName,
                        earlier.getPos()));
            } else {
                table.declare(c);
            }
            cd.symbol = c;
        }

        for (Tree.ClassDef cd : program.classes) {
            Class c = cd.symbol;
            if (cd.parent.isPresent() && c.getParent() == null) {
                issue(new ClassNotFoundError(cd.getLocation(), cd.parent.get().name));
                c.dettachParent();
            }
            if (calcOrder(c) <= calcOrder(c.getParent())) {
                issue(new BadInheritanceError(cd.getLocation()));
                c.dettachParent();
            }
        }

        for (Tree.ClassDef cd : program.classes) {
            cd.symbol.createType();
        }

        for (Tree.ClassDef cd : program.classes) {
            cd.accept(this);
            if (cd.id.name.equals("Main")) {
                program.main = cd.symbol;
            }
        }

        for (Tree.ClassDef cd : program.classes) {
            checkOverride(cd.symbol);
        }

        if (!isMainClass(program.main)) {
            issue(new NoMainClassError());
        }
        table.close();
    }

    // visiting declarations
    @Override
    public void visitClassDef(Tree.ClassDef classDef) {
        table.open(classDef.symbol.getAssociatedScope());
        for (var f : classDef.fields) {
            f.accept(this);
        }
        table.close();
    }

    @Override
    public void visitVarDef(Tree.VarDef varDef) {
        varDef.typeLit.accept(this);
        if (varDef.typeLit.type.equal(BaseType.VOID)) {
            issue(new BadVarTypeError(varDef.getLocation(), varDef.id.name));
            // for argList
            varDef.symbol = new Variable(".error", BaseType.ERROR, varDef
                    .getLocation());
            return;
        }
        Variable v = new Variable(varDef.id.name, varDef.typeLit.type,
                varDef.getLocation());
        Symbol sym = table.lookup(varDef.id.name, true);
        if (sym != null) {
            if (table.getCurrentScope().equals(sym.getScope())) {
                issue(new DeclConflictError(v.getPos(), v.getName(),
                        sym.getPos()));
            } else if ((sym.getScope().isFormalScope() || sym.getScope()
                    .isLocalScope())) {
                issue(new DeclConflictError(v.getPos(), v.getName(),
                        sym.getPos()));
            } else {
                table.declare(v);
            }
        } else {
            table.declare(v);
        }
        varDef.symbol = v;
    }

    @Override
    public void visitMethodDef(Tree.MethodDef funcDef) {
        funcDef.returnType.accept(this);
        Function f = new Function(funcDef.isStatic, funcDef.id.name,
                funcDef.returnType.type, funcDef.body, funcDef.getLocation());
        funcDef.symbol = f;
        Symbol sym = table.lookup(funcDef.id.name, false);
        if (sym != null) {
            issue(new DeclConflictError(funcDef.getLocation(),
                    funcDef.id.name, sym.getPos()));
        } else {
            table.declare(f);
        }
        table.open(f.getAssociatedScope());
        for (var d : funcDef.params) {
            d.accept(this);
            f.appendParam(d.symbol);
        }
        funcDef.body.accept(this);
        table.close();
    }

    // visiting types
    @Override
    public void visitTInt(Tree.TInt that) {
        that.type = BaseType.INT;
    }

    @Override
    public void visitTBool(Tree.TBool that) {
        that.type = BaseType.BOOL;
    }

    @Override
    public void visitTString(Tree.TString that) {
        that.type = BaseType.STRING;
    }

    @Override
    public void visitTVoid(Tree.TVoid that) {
        that.type = BaseType.VOID;
    }

    @Override
    public void visitTClass(Tree.TClass typeClass) {
        Class c = table.lookupClass(typeClass.id.name);
        if (c == null) {
            issue(new ClassNotFoundError(typeClass.getLocation(),
                    typeClass.id.name));
            typeClass.type = BaseType.ERROR;
        } else {
            typeClass.type = c.getType();
        }
    }

    @Override
    public void visitTArray(Tree.TArray typeArray) {
        typeArray.elemType.accept(this);
        if (typeArray.elemType.type.equal(BaseType.ERROR)) {
            typeArray.type = BaseType.ERROR;
        } else if (typeArray.elemType.type.equal(BaseType.VOID)) {
            issue(new BadArrElementError(typeArray.getLocation()));
            typeArray.type = BaseType.ERROR;
        } else {
            typeArray.type = new decaf.type.ArrayType(
                    typeArray.elemType.type);
        }
    }

    // for VarDecl in LocalScope
    @Override
    public void visitBlock(Tree.Block block) {
        block.associatedScope = new LocalScope(block);
        table.open(block.associatedScope);
        for (var s : block.block) {
            s.accept(this);
        }
        table.close();
    }

    @Override
    public void visitFor(Tree.For forLoop) {
        forLoop.body.accept(this);
    }

    @Override
    public void visitIf(Tree.If ifStmt) {
        ifStmt.trueBranch.accept(this);
        ifStmt.falseBranch.ifPresent(b -> b.accept(this));
    }

    @Override
    public void visitWhile(Tree.While aWhile) {
        aWhile.body.accept(this);
    }

    private int calcOrder(Class c) {
        if (c == null) {
            return -1;
        }
        if (c.getOrder() < 0) {
            c.setOrder(0);
            c.setOrder(calcOrder(c.getParent()) + 1);
        }
        return c.getOrder();
    }

    private void checkOverride(Class c) {
        if (c.isCheck()) {
            return;
        }
        Class parent = c.getParent();
        if (parent == null) {
            return;
        }
        checkOverride(parent);

        ClassScope parentScope = parent.getAssociatedScope();
        ClassScope subScope = c.getAssociatedScope();
        table.open(parentScope);
        Iterator<Symbol> iter = subScope.iterator();
        while (iter.hasNext()) {
            Symbol suspect = iter.next();
            Symbol sym = table.lookup(suspect.getName(), true);
            if (sym != null && !sym.isClass()) {
                if ((suspect.isVariable() && sym.isFunction())
                        || (suspect.isFunction() && sym.isVariable())) {
                    issue(new DeclConflictError(suspect.getPos(),
                            suspect.getName(), sym.getPos()));
                    iter.remove();
                } else if (suspect.isFunction()) {
                    if (((Function) suspect).isStatik()
                            || ((Function) sym).isStatik()) {
                        issue(new DeclConflictError(suspect.getPos(),
                                suspect.getName(), sym.getPos()));
                        iter.remove();
                    } else if (!suspect.getType().compatible(sym.getType())) {
                        issue(new BadOverrideError(suspect.getPos(),
                                suspect.getName(),
                                ((ClassScope) sym.getScope()).getOwner()
                                        .getName()));
                        iter.remove();
                    }
                } else if (suspect.isVariable()) {
                    issue(new OverridingVarError(suspect.getPos(),
                            suspect.getName()));
                    iter.remove();
                }
            }
        }
        table.close();
        c.setCheck(true);
    }

    private boolean isMainClass(Class c) {
        if (c == null) {
            return false;
        }
        table.open(c.getAssociatedScope());
        Symbol main = table.lookup("main", false);
        if (main == null || !main.isFunction()) {
            return false;
        }
        ((Function) main).setMain(true);
        FuncType type = (FuncType) main.getType();
        return type.getReturnType().equal(BaseType.VOID)
                && type.numOfParams() == 0 && ((Function) main).isStatik();
    }
}
