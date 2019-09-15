package decaf.lowlevel.tac;

import decaf.lowlevel.Label;
import decaf.lowlevel.TacInstr;
import decaf.lowlevel.Temp;

import java.util.List;

public class MethodVisitor {

    public void visitAssign(Temp dst, Temp src) {
        _func.add(new TacInstr.Assign(dst, src));
    }

    public Temp visitLoad(int value) {
        var temp = freshTemp();
        _func.add(new TacInstr.LoadImm4(temp, value));
        return temp;
    }

    public Temp visitLoad(boolean value) {
        var temp = freshTemp();
        _func.add(new TacInstr.LoadImm4(temp, value ? 1 : 0));
        return temp;
    }

    public Temp visitLoad(String value) {
        var temp = freshTemp();
        _func.add(new TacInstr.LoadStrConst(temp, value));
        return temp;
    }

    public Temp visitLoadVTable(String clazz) {
        var temp = freshTemp();
        _func.add(new TacInstr.LoadVTbl(temp, _ctx.getVTable(clazz)));
        return temp;
    }

    public Temp visitUnary(TacInstr.Unary.Op op, Temp operand) {
        var temp = freshTemp();
        _func.add(new TacInstr.Unary(op, temp, operand));
        return temp;
    }

    public void visitUnarySelf(TacInstr.Unary.Op op, Temp self) {
        _func.add(new TacInstr.Unary(op, self, self));
    }

    public Temp visitBinary(TacInstr.Binary.Op op, Temp lhs, Temp rhs) {
        var temp = freshTemp();
        _func.add(new TacInstr.Binary(op, temp, lhs, rhs));
        return temp;
    }

    public void visitBinarySelf(TacInstr.Binary.Op op, Temp self, Temp operand) {
        _func.add(new TacInstr.Binary(op, self, self, operand));
    }

    public void visitBranch(Label target) {
        _func.add(new TacInstr.Branch(target));
    }

    public void visitBranch(TacInstr.CondBranch.Op op, Temp cond, Label target) {
        _func.add(new TacInstr.CondBranch(op, cond, target));
    }

    public void visitReturn() {
        _func.add(new TacInstr.Return());
    }

    public void visitReturn(Temp value) {
        _func.add(new TacInstr.Return(value));
    }

    public Temp visitNewClass(String clazz) {
        var temp = freshTemp();
        var entry = _ctx.getConstructorLabel(clazz);
        _func.add(new TacInstr.DirectCall(temp, entry));
        return temp;
    }

    public Temp visitMemberAccess(Temp object, String clazz, String variable) {
        return visitLoadFrom(object, _ctx.getOffset(clazz, variable));
    }

    public void visitMemberWrite(Temp object, String clazz, String variable, Temp value) {
        visitStoreTo(object, _ctx.getOffset(clazz, variable), value);
    }

    public Temp visitMemberCall(Temp object, String clazz, String method, List<Temp> args) {
        var temp = freshTemp();
        var vtbl = visitLoadFrom(object);
        var entry = visitLoadFrom(vtbl, _ctx.getOffset(clazz, method));

        _func.add(new TacInstr.Parm(object));
        for (var arg : args) {
            _func.add(new TacInstr.Parm(arg));
        }
        _func.add(new TacInstr.IndirectCall(temp, entry));
        return temp;
    }

    public Temp visitStaticCall(String clazz, String method, List<Temp> args) {
        var temp = freshTemp();
        var entry = _ctx.getMethodLabel(clazz, method);

        for (var arg : args) {
            _func.add(new TacInstr.Parm(arg));
        }
        _func.add(new TacInstr.DirectCall(temp, entry));
        return temp;
    }

    public Temp visitIntrinsicCall(Intrinsic func, Temp... args) {
        var temp = freshTemp();

        for (var arg : args) {
            _func.add(new TacInstr.Parm(arg));
        }
        _func.add(new TacInstr.DirectCall(temp, func));
        return temp;
    }

    public void visitPrint(String msg) {
        visitIntrinsicCall(Intrinsic.PRINT_STRING, visitLoad(msg));
    }

    public Temp visitLoadFrom(Temp base, int offset) {
        var temp = freshTemp();
        _func.add(new TacInstr.Memory(TacInstr.Memory.Op.LOAD, temp, base, offset));
        return temp;
    }

    public Temp visitLoadFrom(Temp base) {
        return visitLoadFrom(base, 0);
    }

    public void visitStoreTo(Temp base, int offset, Temp value) {
        _func.add(new TacInstr.Memory(TacInstr.Memory.Op.STORE, value, base, offset));
    }

    public void visitStoreTo(Temp addr, Temp value) {
        visitStoreTo(addr, 0, value);
    }

    public void visitLabel(Label lbl) {
        _func.add(new TacInstr.Mark(lbl));
    }

    public void visitComment(String content) {
        _func.add(new TacInstr.Memo(content));
    }

    public void visitRaw(TacInstr instr) {
        _func.add(instr);
    }

    public void visitEnd() {
        // Make sure that every function ends with a return instruction.
        if (_func.instrSeq.isEmpty() || !_func.instrSeq.get(_func.instrSeq.size() - 1).isReturn()) {
            _func.add(new TacInstr.Return());
        }
        _func.tempUsed = getUsedTemp();
        _ctx.funcs.add(_func);
    }

    public Label freshLabel() {
        return _ctx.freshLabel();
    }

    public Temp freshTemp() {
        var temp = new Temp(_next_temp_id);
        _next_temp_id++;
        return temp;
    }

    public Temp getArgTemp(int index) {
        return _args_temps[index];
    }

    public int getUsedTemp() {
        return _next_temp_id;
    }

    MethodVisitor(Label entry, int numArgs, ProgramWriter.Context ctx) {
        _ctx = ctx;
        _func = new TAC.Func(entry, numArgs);
        visitLabel(entry);
        _args_temps = new Temp[numArgs];
        for (int i = 0; i < numArgs; i++) {
            _args_temps[i] = freshTemp();
        }
    }

    private TAC.Func _func;

    private ProgramWriter.Context _ctx;

    private int _next_temp_id = 0;

    private Temp[] _args_temps;
}
