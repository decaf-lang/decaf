package decaf.typecheck;

import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.error.*;
import decaf.scope.*;
import decaf.symbol.ClassSymbol;
import decaf.symbol.MethodSymbol;
import decaf.symbol.VarSymbol;
import decaf.tree.Tree;
import decaf.type.BuiltInType;
import decaf.type.ClassType;
import decaf.type.FunType;
import decaf.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class Namer extends Phase<Tree.TopLevel, Tree.TopLevel> implements TypeLitVisited {

    public Namer(Config config) {
        super("namer", config);
    }

    @Override
    public Tree.TopLevel transform(Tree.TopLevel tree) {
        tree.globalScope = new GlobalScope();
        tree.table = new ScopeStack(tree.globalScope);
        tree.accept(this, tree.table);
        return tree;
    }

    @Override
    public void visitTopLevel(Tree.TopLevel program, ScopeStack ctx) {
        var classes = new HashMap<String, Tree.ClassDef>();

        // Check conflicting definitions. If any, ignore the redefined ones.
        for (var clazz : program.classes) {
            var earlier = classes.get(clazz.name);
            if (earlier != null) {
                issue(new DeclConflictError(clazz.pos, clazz.name, earlier.pos));
            } else {
                classes.put(clazz.name, clazz);
            }
        }

        // Make sure the base class exists. If not, ignore the inheritance.
        for (var clazz : classes.values()) {
            clazz.parent.ifPresent(p -> {
                if (classes.containsKey(p.name)) { // good
                    clazz.superClass = classes.get(p.name);
                } else { // bad
                    issue(new ClassNotFoundError(clazz.pos, p.name));
                    clazz.parent = Optional.empty();
                }
            });
        }

        // Make sure any inheritance does not form a cycle.
        checkCycles(classes);
        // If so, return with errors.
        if (hasError()) return;

        // So far, class inheritance is well-formed, i.e. inheritance relations form a forest of trees. Now we need to
        // resolve every class definition, make sure that every member (variable/method) is well-typed.
        // Realizing that a class type can be used in the definition of a class member, either a variable or a method,
        // we shall first know all the accessible class types in the program. These types are wrapped into `ClassSymbol`s.
        // Note that currently, the associated `scope` is empty because member resolving has not started
        // yet. All class symbols are stored in the global scope.
        for (var clazz : classes.values()) {
            createClassSymbol(clazz, ctx.global);
        }

        // Now, we can resolve every class definition to fill in its class scope table. To check if the overriding
        // behaves correctly, we should first resolve super class and then its subclasses.
        for (var clazz : classes.values()) {
            clazz.accept(this, ctx);
        }

        // Finally, let's locate the main class, whose name is 'Main', and contains a method like:
        //  static void main() { ... }
        boolean found = false;
        for (var clazz : classes.values()) {
            if (clazz.name.equals("Main")) {
                var symbol = clazz.symbol.scope.find("main");
                if (symbol.isPresent() && symbol.get().isMethodSymbol()) {
                    var method = (MethodSymbol) symbol.get();
                    if (method.isStatic() && method.getReturnType().isVoidType() && method.getFunType().arity() == 0) {
                        method.setMain();
                        program.main = clazz.symbol;
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            issue(new NoMainClassError());
        }
    }

    private void checkCycles(HashMap<String, Tree.ClassDef> classes) {
        var visitedTime = new HashMap<String, Integer>();
        for (var clazz : classes.values()) {
            visitedTime.put(clazz.name, 0);
        }

        var time = 1; // nodes in the same inheritance path/chain have the same time
        Tree.ClassDef from = null;
        for (var node : classes.keySet()) {
            if (visitedTime.get(node) != 0) { // already done, skip
                continue;
            }

            // visit from this node
            while (true) {
                if (visitedTime.get(node) == 0) { // not visited yet
                    visitedTime.put(node, time);
                    var clazz = classes.get(node);
                    if (clazz.parent.isPresent()) {
                        // continue to visit its parent
                        node = clazz.parent.get().name;
                        from = clazz;
                    } else break;
                } else if (visitedTime.get(node) == time) { // find a cycle
                    issue(new BadInheritanceError(from.pos));
                    break;
                } else { // this node is visited earlier, also done
                    break;
                }
            }
            time++;
        }
    }

    private void createClassSymbol(Tree.ClassDef clazz, GlobalScope global) {
        if (global.containsKey(clazz.name)) return;

        if (clazz.parent.isPresent()) {
            createClassSymbol(clazz.superClass, global);
            var base = global.getClass(clazz.parent.get().name);
            var type = new ClassType(clazz.name, base.type);
            var scope = new ClassScope(base.scope);
            var symbol = new ClassSymbol(clazz.name, base, type, scope, clazz.pos);
            global.declare(symbol);
            clazz.symbol = symbol;
        } else {
            var type = new ClassType(clazz.name);
            var scope = new ClassScope();
            var symbol = new ClassSymbol(clazz.name, type, scope, clazz.pos);
            global.declare(symbol);
            clazz.symbol = symbol;
        }
    }

    @Override
    public void visitClassDef(Tree.ClassDef clazz, ScopeStack ctx) {
        if (clazz.resolved) return;

        if (clazz.hasParent()) {
            clazz.superClass.accept(this, ctx);
        }

        ctx.open(clazz.symbol.scope);
        for (var field : clazz.fields) {
            field.accept(this, ctx);
        }
        ctx.close();
        clazz.resolved = true;
    }

    @Override
    public void visitVarDef(Tree.VarDef varDef, ScopeStack ctx) {
        var earlier = ctx.findConflict(varDef.name);
        if (earlier.isPresent()) {
            if (earlier.get().isVarSymbol()) {
                issue(new OverridingVarError(varDef.pos, varDef.name));
            } else {
                issue(new DeclConflictError(varDef.pos, varDef.name, earlier.get().pos));
            }
            return;
        }

        varDef.typeLit.accept(this, ctx);
        if (varDef.typeLit.type.eq(BuiltInType.VOID)) {
            issue(new BadVarTypeError(varDef.pos, varDef.name));
            return;
        }

        if (varDef.typeLit.type.noError()) {
            var symbol = new VarSymbol(varDef.name, varDef.typeLit.type, varDef.pos);
            ctx.declare(symbol);
            varDef.symbol = symbol;
        }
    }

    @Override
    public void visitMethodDef(Tree.MethodDef method, ScopeStack ctx) {
        var earlier = ctx.findConflict(method.name);
        if (earlier.isPresent()) {
            if (earlier.get().isMethodSymbol()) { // may be overriden
                var suspect = (MethodSymbol) earlier.get();
                if (!suspect.isStatic() && !method.isStatic()) {
                    // Only non-static methods can be overriden, but the type signature must be equivalent.
                    var formal = new FormalScope();
                    typeMethod(method, ctx, formal);
                    if (method.type.noError() && method.type.subtypeOf(suspect.type)) { // override success
                        var symbol = new MethodSymbol(method.name, method.type, formal, method.pos, method.modifiers,
                                ctx.currentClass());
                        ctx.declare(symbol);
                        method.symbol = symbol;
                        ctx.open(formal);
                        method.body.accept(this, ctx);
                        ctx.close();
                    } else {
                        issue(new BadOverrideError(method.pos, method.name, suspect.owner.name));
                    }
                }

                return;
            }

            issue(new DeclConflictError(method.pos, method.name, earlier.get().pos));
            return;
        }

        var formal = new FormalScope();
        typeMethod(method, ctx, formal);
        if (method.type.noError()) {
            var symbol = new MethodSymbol(method.name, method.type, formal, method.pos, method.modifiers,
                    ctx.currentClass());
            ctx.declare(symbol);
            method.symbol = symbol;
            ctx.open(formal);
            method.body.accept(this, ctx);
            ctx.close();
        }
    }

    private void typeMethod(Tree.MethodDef method, ScopeStack ctx, FormalScope formal) {
        method.returnType.accept(this, ctx);
        if (method.returnType.type.noError()) {
            ctx.open(formal);
            if (!method.isStatic()) ctx.declare(VarSymbol.thisVar(ctx.currentClass().type, method.id.pos));
            var argTypes = new ArrayList<Type>();
            for (var param : method.params) {
                param.accept(this, ctx);
                argTypes.add(param.typeLit.type);
            }
            method.type = new FunType(method.returnType.type, argTypes);
            ctx.close();
        }
    }

    @Override
    public void visitBlock(Tree.Block block, ScopeStack ctx) {
        block.scope = new LocalScope(block);
        ctx.open(block.scope);
        for (var stmt : block.stmts) {
            stmt.accept(this, ctx);
        }
        ctx.close();
    }

    @Override
    public void visitLocalVarDef(Tree.LocalVarDef varDef, ScopeStack ctx) { // TODO merge with VarDef
        var earlier = ctx.findConflict(varDef.name);
        if (earlier.isPresent()) {
            if (earlier.get().isVarSymbol()) {
                issue(new OverridingVarError(varDef.pos, varDef.name));
            } else {
                issue(new DeclConflictError(varDef.pos, varDef.name, earlier.get().pos));
            }
            return;
        }

        varDef.typeLit.accept(this, ctx);
        if (varDef.typeLit.type.eq(BuiltInType.VOID)) {
            issue(new BadVarTypeError(varDef.pos, varDef.name));
            return;
        }

        if (varDef.typeLit.type.noError()) {
            var symbol = new VarSymbol(varDef.name, varDef.typeLit.type, varDef.pos);
            ctx.declare(symbol);
            varDef.symbol = symbol;
        }
    }

    @Override
    public void visitFor(Tree.For loop, ScopeStack ctx) {
        loop.body.accept(this, ctx);
    }

    @Override
    public void visitIf(Tree.If stmt, ScopeStack ctx) {
        stmt.trueBranch.accept(this, ctx);
        stmt.falseBranch.ifPresent(b -> b.accept(this, ctx));
    }

    @Override
    public void visitWhile(Tree.While loop, ScopeStack ctx) {
        loop.body.accept(this, ctx);
    }

}
