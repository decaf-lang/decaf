package decaf.tools.tac;

import java.util.List;

public class MethodVisitor {

    public void visitAssign(Tac.Temp dst, Tac.Temp src) {
        _func.add(new Tac.Assign(dst, src));
    }

    public Tac.Temp visitLoad(int value) {
        var temp = freshTemp();
        _func.add(new Tac.LoadImm4(temp, value));
        return temp;
    }

    public Tac.Temp visitLoad(boolean value) {
        var temp = freshTemp();
        _func.add(new Tac.LoadImm4(temp, value ? 1 : 0));
        return temp;
    }

    public Tac.Temp visitLoad(String value) {
        var temp = freshTemp();
        _func.add(new Tac.LoadStrConst(temp, value));
        return temp;
    }

    public Tac.Temp visitLoadVTable(String clazz) {
        var temp = freshTemp();
        _func.add(new Tac.LoadVTbl(temp, _ctx.getVTable(clazz)));
        return temp;
    }

    public Tac.Temp visitUnary(Tac.Unary.Op op, Tac.Temp operand) {
        var temp = freshTemp();
        _func.add(new Tac.Unary(op, temp, operand));
        return temp;
    }

    public void visitUnarySelf(Tac.Unary.Op op, Tac.Temp self) {
        _func.add(new Tac.Unary(op, self, self));
    }

    public Tac.Temp visitBinary(Tac.Binary.Op op, Tac.Temp lhs, Tac.Temp rhs) {
        var temp = freshTemp();
        _func.add(new Tac.Binary(op, temp, lhs, rhs));
        return temp;
    }

    public void visitBinarySelf(Tac.Binary.Op op, Tac.Temp self, Tac.Temp operand) {
        _func.add(new Tac.Binary(op, self, self, operand));
    }

    public void visitBranch(Tac.Label target) {
        _func.add(new Tac.Branch(target));
    }

    public void visitBranch(Tac.ConditionalBranch.Op op, Tac.Temp cond, Tac.Label target) {
        _func.add(new Tac.ConditionalBranch(op, cond, target));
    }

    public void visitReturn() {
        _func.add(new Tac.Return());
    }

    public void visitReturn(Tac.Temp value) {
        _func.add(new Tac.Return(value));
    }

    public Tac.Temp visitNewClass(String clazz) {
        var temp = freshTemp();
        var entry = _ctx.getConstructorLabel(clazz);
        _func.add(new Tac.DirectCall(temp, entry));
        return temp;
    }

    public Tac.Temp visitMemberAccess(Tac.Temp object, String clazz, String variable) {
        return visitLoadFrom(object, _ctx.getOffset(clazz, variable));
    }

    public void visitMemberWrite(Tac.Temp object, String clazz, String variable, Tac.Temp value) {
        visitStoreTo(object, _ctx.getOffset(clazz, variable), value);
    }

    public Tac.Temp visitMemberCall(Tac.Temp object, String clazz, String method, List<Tac.Temp> args) {
        var temp = freshTemp();
        var vtbl = visitLoadFrom(object);
        var entry = visitLoadFrom(vtbl, _ctx.getOffset(clazz, method));

        _func.add(new Tac.Parm(object));
        for (var arg : args) {
            _func.add(new Tac.Parm(arg));
        }
        _func.add(new Tac.IndirectCall(temp, entry));
        return temp;
    }

    public Tac.Temp visitStaticCall(String clazz, String method, List<Tac.Temp> args) {
        var temp = freshTemp();
        var entry = _ctx.getMethodLabel(clazz, method);

        for (var arg : args) {
            _func.add(new Tac.Parm(arg));
        }
        _func.add(new Tac.DirectCall(temp, entry));
        return temp;
    }

    public Tac.Temp visitIntrinsicCall(Intrinsic func, Tac.Temp... args) {
        var temp = freshTemp();

        for (var arg : args) {
            _func.add(new Tac.Parm(arg));
        }
        _func.add(new Tac.DirectCall(temp, func.entry));
        return temp;
    }

    public void visitPrint(String msg) {
        visitIntrinsicCall(Intrinsic.PRINT_STRING, visitLoad(msg));
    }

    public Tac.Temp visitLoadFrom(Tac.Temp base, int offset) {
        var temp = freshTemp();
        _func.add(new Tac.Memory(Tac.Memory.Op.LOAD, temp, base, offset));
        return temp;
    }

    public Tac.Temp visitLoadFrom(Tac.Temp base) {
        return visitLoadFrom(base, 0);
    }

    public void visitStoreTo(Tac.Temp base, int offset, Tac.Temp value) {
        _func.add(new Tac.Memory(Tac.Memory.Op.STORE, value, base, offset));
    }

    public void visitStoreTo(Tac.Temp addr, Tac.Temp value) {
        visitStoreTo(addr, 0, value);
    }

    public void visitLabel(Tac.Label lbl) {
        _func.add(new Tac.Mark(lbl));
    }

    public void visitComment(String content) {
        _func.add(new Tac.Memo(content));
    }

    public void visitRaw(Tac.Instr instr) {
        _func.add(instr);
    }

    public void visitEnd() {
        _func.tempUsed = getUsedTemp();
        _ctx.funcs.add(_func);
    }

    public Tac.Label freshLabel() {
        return _ctx.freshLabel();
    }

    public Tac.Temp freshTemp() {
        var temp = new Tac.Temp(_next_temp_id);
        _next_temp_id++;
        return temp;
    }

    public Tac.Temp getArgTemp(int index) {
        return _args_temps[index];
    }

    public int getUsedTemp() {
        return _next_temp_id;
    }

    MethodVisitor(Tac.Label entry, int numArgs, ProgramWriter.Context ctx) {
        _ctx = ctx;
        _func = new Tac.Func(entry);
        visitLabel(entry);
        _args_temps = new Tac.Temp[numArgs];
        for (int i = 0; i < numArgs; i++) {
            _args_temps[i] = freshTemp();
        }
    }

    private Tac.Func _func;

    private ProgramWriter.Context _ctx;

    private int _next_temp_id = 0;

    private Tac.Temp[] _args_temps;
}
