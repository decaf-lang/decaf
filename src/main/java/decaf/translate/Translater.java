package decaf.translate;

import decaf.backend.OffsetCounter;
import decaf.error.RuntimeError;
import decaf.machdesc.Intrinsic;
import decaf.scope.ClassScope;
import decaf.symbol.ClassSymbol;
import decaf.symbol.MethodSymbol;
import decaf.symbol.Symbol;
import decaf.symbol.VarSymbol;
import decaf.tac.*;
import decaf.tree.Tree;
import decaf.type.BuiltInType;
import decaf.type.Type;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Context {
}

public class Translater {
    private List<VTable> vtables;

    private List<Functy> funcs;

    private Functy currentFuncty;

    public Translater() {
        vtables = new ArrayList<VTable>();
        funcs = new ArrayList<Functy>();
    }

    public static Translater translate(Tree.TopLevel tree) {
        Translater tr = new Translater();
        TransPass1 tp1 = new TransPass1(tr);
        var ctx = new Context();
        tp1.visitTopLevel(tree, ctx);
        TransPass2 tp2 = new TransPass2(tr);
        tp2.visitTopLevel(tree, ctx);
        return tr;
    }

    public void printTo(PrintWriter pw) {
        for (VTable vt : vtables) {
            pw.println("VTABLE(" + vt.name + ") {");
            if (vt.parent != null) {
                pw.println("    " + vt.parent.name);
            } else {
                pw.println("    <empty>");
            }
            pw.println("    " + vt.className);
            for (Label l : vt.entries) {
                pw.println("    " + l.name + ";");
            }
            pw.println("}");
            pw.println();
        }
        for (Functy ft : funcs) {
            pw.println("FUNCTION(" + ft.label.name + ") {");
            pw.println(ft.paramMemo);
            Tac tac = ft.head;
            while (tac != null) {
                if (tac.opc == Tac.Kind.MARK) {
                    pw.println(tac);
                } else {
                    pw.println("    " + tac);
                }
                tac = tac.next;
            }
            pw.println("}");
            pw.println();
        }
    }

    public List<VTable> getVtables() {
        return vtables;
    }

    public List<Functy> getFuncs() {
        return funcs;
    }

    public void createFuncty(MethodSymbol func) {
        Functy functy = new Functy();
        if (func.isMain()) {
            functy.label = Label.createLabel("main", true);
        } else {
            functy.label = Label.createLabel("_"
                    + ((ClassScope) func.getScope()).getOwner().name + "."
                    + func.name, true);
        }
        functy.sym = func;
        func.setFuncty(functy);
    }

    public void beginFunc(MethodSymbol func) {
        currentFuncty = func.getFuncty();
        currentFuncty.paramMemo = memoOf(func);
        genMark(func.getFuncty().label);
    }

    public void endFunc() {
        funcs.add(currentFuncty);
        currentFuncty = null;
    }

    private Tac memoOf(MethodSymbol func) {
        StringBuilder sb = new StringBuilder();
        Iterator<Symbol> iter = func.scope.iterator();
        while (iter.hasNext()) {
            VarSymbol v = (VarSymbol) iter.next();
            Temp t = v.getTemp();
            t.offset = v.getOffset();
            sb.append(t.name + ":" + t.offset + " ");
        }
        if (sb.length() > 0) {
            return Tac.genMemo(sb.substring(0, sb.length() - 1));
        } else {
            return Tac.genMemo("");
        }
    }

    public void createVTable(ClassSymbol c) {
        if (c.getVtable() != null) {
            return;
        }
        VTable vtable = new VTable();
        vtable.className = c.name;
        vtable.name = "_" + c.name;
        vtable.entries = new Label[c.getNumNonStaticFunc()];
        fillVTableEntries(vtable, c.scope);
        c.setVtable(vtable);
        vtables.add(vtable);
    }

    private void fillVTableEntries(VTable vt, ClassScope cs) {
        if (cs.parentScope.isPresent()) {
            fillVTableEntries(vt, cs.parentScope.get());
        }

        Iterator<Symbol> iter = cs.iterator();
        while (iter.hasNext()) {
            Symbol sym = iter.next();
            if (sym.isMethodSymbol() && !((MethodSymbol) sym).isStatic()) {
                MethodSymbol func = (MethodSymbol) sym;
                vt.entries[func.getOrder()] = func.getFuncty().label;
            }
        }
    }

    public void append(Tac tac) {
        if (currentFuncty.head == null) {
            currentFuncty.head = currentFuncty.tail = tac;
        } else {
            tac.prev = currentFuncty.tail;
            currentFuncty.tail.next = tac;
            currentFuncty.tail = tac;
        }
    }

    public Temp genAdd(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genAdd(dst, src1, src2));
        return dst;
    }

    public Temp genSub(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genSub(dst, src1, src2));
        return dst;
    }

    public Temp genMul(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genMul(dst, src1, src2));
        return dst;
    }

    public Temp genDiv(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genDiv(dst, src1, src2));
        return dst;
    }

    public Temp genMod(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genMod(dst, src1, src2));
        return dst;
    }

    public Temp genNeg(Temp src) {
        Temp dst = Temp.createTempI4();
        append(Tac.genNeg(dst, src));
        return dst;
    }

    public Temp genLAnd(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLAnd(dst, src1, src2));
        return dst;
    }

    public Temp genLOr(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLOr(dst, src1, src2));
        return dst;
    }

    public Temp genLNot(Temp src) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLNot(dst, src));
        return dst;
    }

    public Temp genGtr(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genGtr(dst, src1, src2));
        return dst;
    }

    public Temp genGeq(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genGeq(dst, src1, src2));
        return dst;
    }

    public Temp genEqu(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genEqu(dst, src1, src2));
        return dst;
    }

    public Temp genNeq(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genNeq(dst, src1, src2));
        return dst;
    }

    public Temp genLeq(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLeq(dst, src1, src2));
        return dst;
    }

    public Temp genLes(Temp src1, Temp src2) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLes(dst, src1, src2));
        return dst;
    }

    public void genAssign(Temp dst, Temp src) {
        append(Tac.genAssign(dst, src));
    }

    public Temp genLoadVTable(VTable vtbl) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLoadVtbl(dst, vtbl));
        return dst;
    }

    public Temp genIndirectCall(Temp func, Type retType) {
        Temp dst;
        if (retType.eq(BuiltInType.VOID)) {
            dst = null;
        } else {
            dst = Temp.createTempI4();
        }
        append(Tac.genIndirectCall(dst, func));
        return dst;
    }

    public Temp genDirectCall(Label func, Type retType) {
        Temp dst;
        if (retType.eq(BuiltInType.VOID)) {
            dst = null;
        } else {
            dst = Temp.createTempI4();
        }
        append(Tac.genDirectCall(dst, func));
        return dst;
    }

    public Temp genIntrinsicCall(Intrinsic intrn) {
        Temp dst;
        if (intrn.type.eq(BuiltInType.VOID)) {
            dst = null;
        } else {
            dst = Temp.createTempI4();
        }
        append(Tac.genDirectCall(dst, intrn.label));
        return dst;
    }

    public void genReturn(Temp src) {
        append(Tac.genReturn(src));
    }

    public void genBranch(Label dst) {
        append(Tac.genBranch(dst));
    }

    public void genBeqz(Temp cond, Label dst) {
        append(Tac.genBeqz(cond, dst));
    }

    public void genBnez(Temp cond, Label dst) {
        append(Tac.genBnez(cond, dst));
    }

    public Temp genLoad(Temp base, int offset) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLoad(dst, base, Temp.createConstTemp(offset)));
        return dst;
    }

    public void genStore(Temp src, Temp base, int offset) {
        append(Tac.genStore(src, base, Temp.createConstTemp(offset)));
    }

    public Temp genLoadImm4(int imm) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLoadImm4(dst, Temp.createConstTemp(imm)));
        return dst;
    }

    public Temp genLoadStrConst(String value) {
        Temp dst = Temp.createTempI4();
        append(Tac.genLoadStrConst(dst, value));
        return dst;
    }

    public void genMemo(String comment) {
        append(Tac.genMemo(comment));
    }

    public void genMark(Label label) {
        append(Tac.genMark(label));
    }

    public void genParm(Temp parm) {
        append(Tac.genParm(parm));
    }

    public void genCheckArrayIndex(Temp array, Temp index) {
        Temp length = genLoad(array, -OffsetCounter.WORD_SIZE);
        Temp cond = genLes(index, length);
        Label err = Label.createLabel();
        genBeqz(cond, err);
        cond = genLes(index, genLoadImm4(0));
        Label exit = Label.createLabel();
        genBeqz(cond, exit);
        genMark(err);
        Temp msg = genLoadStrConst(RuntimeError.ARRAY_INDEX_OUT_OF_BOUND);
        genParm(msg);
        genIntrinsicCall(Intrinsic.PRINT_STRING);
        genIntrinsicCall(Intrinsic.HALT);
        genMark(exit);
    }

    public void genCheckNewArraySize(Temp size) {
        Label exit = Label.createLabel();
        Temp cond = genLes(size, genLoadImm4(0));
        genBeqz(cond, exit);
        Temp msg = genLoadStrConst(RuntimeError.NEGATIVE_ARR_SIZE);
        genParm(msg);
        genIntrinsicCall(Intrinsic.PRINT_STRING);
        genIntrinsicCall(Intrinsic.HALT);
        genMark(exit);
    }

    public Temp genNewArray(Temp length) {
        genCheckNewArraySize(length);
        Temp unit = genLoadImm4(OffsetCounter.WORD_SIZE);
        Temp size = genAdd(unit, genMul(unit, length));
        genParm(size);
        Temp obj = genIntrinsicCall(Intrinsic.ALLOCATE);
        genStore(length, obj, 0);
        Label loop = Label.createLabel();
        Label exit = Label.createLabel();
        Temp zero = genLoadImm4(0);
        append(Tac.genAdd(obj, obj, size));
        genMark(loop);
        append(Tac.genSub(size, size, unit));
        genBeqz(size, exit);
        append(Tac.genSub(obj, obj, unit));
        genStore(zero, obj, 0);
        genBranch(loop);
        genMark(exit);
        return obj;
    }

    public void genNewForClass(ClassSymbol c) {
        currentFuncty = new Functy();
        currentFuncty.label = Label.createLabel(
                "_" + c.name + "_" + "New", true);
        c.setNewFuncLabel(currentFuncty.label);
        currentFuncty.paramMemo = Tac.genMemo("");
        genMark(currentFuncty.label);
        Temp size = genLoadImm4(c.getSize());
        genParm(size);
        Temp newObj = genIntrinsicCall(Intrinsic.ALLOCATE);
        int time = c.getSize() / OffsetCounter.WORD_SIZE - 1;
        if (time != 0) {
            Temp zero = genLoadImm4(0);
            if (time < 5) {
                for (int i = 0; i < time; i++) {
                    genStore(zero, newObj, OffsetCounter.WORD_SIZE * (i + 1));
                }
            } else {
                Temp unit = genLoadImm4(OffsetCounter.WORD_SIZE);
                Label loop = Label.createLabel();
                Label exit = Label.createLabel();
                newObj = genAdd(newObj, size);
                genMark(loop);
                genAssign(newObj, genSub(newObj, unit));
                genAssign(size, genSub(size, unit));
                genBeqz(size, exit);
                genStore(zero, newObj, 0);
                genBranch(loop);
                genMark(exit);
            }
        }
        genStore(genLoadVTable(c.getVtable()), newObj, 0);
        genReturn(newObj);
        endFunc();
    }

    public Temp genInstanceof(Temp instance, ClassSymbol c) {
        Temp dst = Temp.createTempI4();
        Label loop = Label.createLabel();
        Label exit = Label.createLabel();
        Temp targetVp = genLoadVTable(c.getVtable());
        Temp vp = genLoad(instance, 0);
        genMark(loop);
        append(Tac.genEqu(dst, targetVp, vp));
        genBnez(dst, exit);
        append(Tac.genLoad(vp, vp, Temp.createConstTemp(0)));
        genBnez(vp, loop);
        append(Tac.genLoadImm4(dst, Temp.createConstTemp(0)));
        genMark(exit);
        return dst;
    }

    public void genClassCast(Temp val, ClassSymbol c) {
        Label loop = Label.createLabel();
        Label exit = Label.createLabel();
        Temp cond = Temp.createTempI4();
        Temp targetVp = genLoadVTable(c.getVtable());
        Temp vp = genLoad(val, 0);
        genMark(loop);
        append(Tac.genEqu(cond, targetVp, vp));
        genBnez(cond, exit);
        append(Tac.genLoad(vp, vp, Temp.createConstTemp(0)));
        genBnez(vp, loop);
        Temp msg = genLoadStrConst(RuntimeError.CLASS_CAST_ERROR1);
        genParm(msg);
        genIntrinsicCall(Intrinsic.PRINT_STRING);
        Temp instanceClassName = genLoad(genLoad(val, 0), 4);
        genParm(instanceClassName);
        genIntrinsicCall(Intrinsic.PRINT_STRING);
        msg = genLoadStrConst(RuntimeError.CLASS_CAST_ERROR2);
        genParm(msg);
        genIntrinsicCall(Intrinsic.PRINT_STRING);
        Temp targetClassName = genLoad(genLoadVTable(c.getVtable()), 4);
        genParm(targetClassName);
        genIntrinsicCall(Intrinsic.PRINT_STRING);
        msg = genLoadStrConst(RuntimeError.CLASS_CAST_ERROR3);
        genParm(msg);
        genIntrinsicCall(Intrinsic.PRINT_STRING);
        genIntrinsicCall(Intrinsic.HALT);
        genMark(exit);
    }
}
