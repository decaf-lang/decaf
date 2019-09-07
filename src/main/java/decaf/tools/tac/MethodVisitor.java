package decaf.tools.tac;

import java.util.List;

public class MethodVisitor {

    public void visitAssign(Temp dst, Temp src) {
        _func.add(new Instr.Assign(dst, src));
    }

    public Temp visitLoad(int value) {
        var temp = freshTemp();
        _func.add(new Instr.LoadImm4(temp, value));
        return temp;
    }

    public Temp visitLoad(boolean value) {
        var temp = freshTemp();
        _func.add(new Instr.LoadImm4(temp, value ? 1 : 0));
        return temp;
    }

    public Temp visitLoad(String value) {
        var temp = freshTemp();
        _func.add(new Instr.LoadStrConst(temp, value));
        return temp;
    }

    public Temp visitLoadVTable(String clazz) {
        var temp = freshTemp();
        _func.add(new Instr.LoadVTbl(temp, _ctx.getVTable(clazz)));
        return temp;
    }

    public Temp visitUnary(Instr.Unary.Op op, Temp operand) {
        var temp = freshTemp();
        _func.add(new Instr.Unary(op, temp, operand));
        return temp;
    }

    public void visitUnarySelf(Instr.Unary.Op op, Temp self) {
        _func.add(new Instr.Unary(op, self, self));
    }

    public Temp visitBinary(Instr.Binary.Op op, Temp lhs, Temp rhs) {
        var temp = freshTemp();
        _func.add(new Instr.Binary(op, temp, lhs, rhs));
        return temp;
    }

    public void visitBinarySelf(Instr.Binary.Op op, Temp self, Temp operand) {
        _func.add(new Instr.Binary(op, self, self, operand));
    }

    public void visitBranch(Label target) {
        _func.add(new Instr.Branch(target));
    }

    public void visitBranch(Instr.ConditionalBranch.Op op, Temp cond, Label target) {
        _func.add(new Instr.ConditionalBranch(op, cond, target));
    }

    public void visitReturn() {
        _func.add(new Instr.Return());
    }

    public void visitReturn(Temp value) {
        _func.add(new Instr.Return(value));
    }

    public Temp visitNewClass(String clazz) {
        var temp = freshTemp();
        var entry = _ctx.getConstructorLabel(clazz);
        _func.add(new Instr.DirectCall(temp, entry));
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

        _func.add(new Instr.Parm(object));
        for (var arg : args) {
            _func.add(new Instr.Parm(arg));
        }
        _func.add(new Instr.IndirectCall(temp, entry));
        return temp;
    }

    public Temp visitStaticCall(String clazz, String method, List<Temp> args) {
        var temp = freshTemp();
        var entry = _ctx.getMethodLabel(clazz, method);

        for (var arg : args) {
            _func.add(new Instr.Parm(arg));
        }
        _func.add(new Instr.DirectCall(temp, entry));
        return temp;
    }

    public Temp visitIntrinsicCall(Intrinsic func, Temp... args) {
        var temp = freshTemp();

        for (var arg : args) {
            _func.add(new Instr.Parm(arg));
        }
        _func.add(new Instr.DirectCall(temp, func.entry));
        return temp;
    }

    public void visitPrint(String msg) {
        visitIntrinsicCall(Intrinsic.PRINT_STRING, visitLoad(msg));
    }

    public Temp visitLoadFrom(Temp base, int offset) {
        var temp = freshTemp();
        _func.add(new Instr.Memory(Instr.Memory.Op.LOAD, temp, base, offset));
        return temp;
    }

    public Temp visitLoadFrom(Temp base) {
        return visitLoadFrom(base, 0);
    }

    public void visitStoreTo(Temp base, int offset, Temp value) {
        _func.add(new Instr.Memory(Instr.Memory.Op.STORE, value, base, offset));
    }

    public void visitStoreTo(Temp addr, Temp value) {
        visitStoreTo(addr, 0, value);
    }

    public void visitLabel(Label lbl) {
        _func.add(new Instr.Mark(lbl));
    }

    public void visitComment(String content) {
        _func.add(new Instr.Memo(content));
    }

    public void visitRaw(Instr instr) {
        _func.add(instr);
    }

    public void visitEnd() {
        _func.tempUsed = getUsedTempCount();
        _ctx.functions.add(_func);
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

    public int getUsedTempCount() {
        return _next_temp_id;
    }

    MethodVisitor(Label entry, int numArgs, ProgramWriter.Context ctx) {
        _ctx = ctx;
        _func = new Function(entry);
        visitLabel(entry);
        _args_temps = new Temp[numArgs];
        for (int i = 0; i < numArgs; i++) {
            _args_temps[i] = freshTemp();
        }
    }

    private Function _func;

    private ProgramWriter.Context _ctx;

    private int _next_temp_id = 0;

    private Temp[] _args_temps;
}
