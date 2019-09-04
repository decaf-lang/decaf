package decaf.translate;

import decaf.backend.OffsetCounter;
import decaf.symbol.ClassSymbol;
import decaf.symbol.MethodSymbol;
import decaf.symbol.Symbol;
import decaf.symbol.VarSymbol;
import decaf.tac.Temp;
import decaf.tree.Tree;
import decaf.tree.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransPass1 implements Visitor<Context> {
    private Translater tr;

    private int objectSize;

    private List<VarSymbol> vars;

    public TransPass1(Translater tr) {
        this.tr = tr;
        vars = new ArrayList<VarSymbol>();
    }

    @Override
    public void visitTopLevel(Tree.TopLevel program, Context ctx) {
        for (Tree.ClassDef cd : program.classes) {
            cd.accept(this, ctx);
        }
        for (Tree.ClassDef cd : program.classes) {
            tr.createVTable(cd.symbol);
            tr.genNewForClass(cd.symbol);
        }
        for (Tree.ClassDef cd : program.classes) {
            if (cd.symbol.baseSymbol.isPresent()) {
                cd.symbol.getVtable().parent = cd.symbol.baseSymbol.get().getVtable();
            }
        }
    }

    @Override
    public void visitClassDef(Tree.ClassDef classDef, Context ctx) {
//        classDef.symbol.resolveFieldOrder(); TODO
        objectSize = 0;
        vars.clear();
        for (var f : classDef.fields) {
            f.accept(this, ctx);
        }
        Collections.sort(vars, Symbol.ORDER_COMPARATOR);
        OffsetCounter oc = OffsetCounter.VARFIELD_OFFSET_COUNTER;
        ClassSymbol c = classDef.symbol.baseSymbol.get();
        if (c != null) {
            oc.set(c.getSize());
        } else {
            oc.reset();
        }
        for (VarSymbol v : vars) {
            v.setOffset(oc.next(OffsetCounter.WORD_SIZE));
        }
    }

    @Override
    public void visitMethodDef(Tree.MethodDef funcDef, Context ctx) {
        MethodSymbol func = funcDef.symbol;
        if (!func.isStatic()) {
            func.setOffset(2 * OffsetCounter.POINTER_SIZE + func.getOrder()
                    * OffsetCounter.POINTER_SIZE);
        }
        tr.createFuncty(func);
        OffsetCounter oc = OffsetCounter.PARAMETER_OFFSET_COUNTER;
        oc.reset();
        int order;
        if (!func.isStatic()) {
            VarSymbol v = (VarSymbol) func.scope.get("this");
            v.setOrder(0);
            Temp t = Temp.createTempI4();
            t.sym = v;
            t.isParam = true;
            v.setTemp(t);
            v.setOffset(oc.next(OffsetCounter.POINTER_SIZE));
            order = 1;
        } else {
            order = 0;
        }
        for (var vd : funcDef.params) {
            vd.symbol.setOrder(order++);
            Temp t = Temp.createTempI4();
            t.sym = vd.symbol;
            t.isParam = true;
            vd.symbol.setTemp(t);
            vd.symbol.setOffset(oc.next(vd.symbol.getTemp().size));
        }
    }

    @Override
    public void visitVarDef(Tree.VarDef varDef, Context ctx) {
        vars.add(varDef.symbol);
        objectSize += OffsetCounter.WORD_SIZE;
    }

}
