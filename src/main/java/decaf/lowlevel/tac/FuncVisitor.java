package decaf.lowlevel.tac;

import decaf.lowlevel.instr.Temp;
import decaf.lowlevel.label.FuncLabel;
import decaf.lowlevel.label.Label;

import java.util.List;

/**
 * Append instructions to a TAC function.
 */
public class FuncVisitor {
    /**
     * Append {@link TacInstr.Assign}.
     *
     * @param dst destination temp
     * @param src source temp
     */
    public void visitAssign(Temp dst, Temp src) {
        func.add(new TacInstr.Assign(dst, src));
    }

    /**
     * Append {@link TacInstr.LoadImm4}.
     *
     * @param value int value
     * @return a fresh temp as destination
     */
    public Temp visitLoad(int value) {
        var temp = freshTemp();
        func.add(new TacInstr.LoadImm4(temp, value));
        return temp;
    }

    /**
     * Append {@link TacInstr.LoadImm4}.
     *
     * @param value boolean value
     * @return a fresh temp as destination
     */
    public Temp visitLoad(boolean value) {
        var temp = freshTemp();
        func.add(new TacInstr.LoadImm4(temp, value ? 1 : 0));
        return temp;
    }

    /**
     * Append {@link TacInstr.LoadStrConst}.
     *
     * @param value string value
     * @return a fresh temp as destination
     */
    public Temp visitLoad(String value) {
        var temp = freshTemp();
        func.add(new TacInstr.LoadStrConst(temp, value));
        return temp;
    }

    /**
     * Append {@link TacInstr.LoadVTbl}.
     *
     * @param clazz clazz name
     * @return a fresh temp as destination
     */
    public Temp visitLoadVTable(String clazz) {
        var temp = freshTemp();
        func.add(new TacInstr.LoadVTbl(temp, ctx.getVTable(clazz)));
        return temp;
    }

    /**
     * Append {@link TacInstr.Unary}.
     *
     * @param op      unary operator, see {@link TacInstr.Unary.Op}
     * @param operand operand temp
     * @return a fresh temp as destination
     */
    public Temp visitUnary(TacInstr.Unary.Op op, Temp operand) {
        var temp = freshTemp();
        func.add(new TacInstr.Unary(op, temp, operand));
        return temp;
    }

    /**
     * Append {@link TacInstr.Unary} of the form {@code self = op self}.
     *
     * @param op   unary operator, see {@link TacInstr.Unary.Op}
     * @param self a temp that is both the destination and the operand
     */
    public void visitUnarySelf(TacInstr.Unary.Op op, Temp self) {
        func.add(new TacInstr.Unary(op, self, self));
    }

    /**
     * Append {@link TacInstr.Binary}.
     *
     * @param op  binary operator, see {@link TacInstr.Binary.Op}
     * @param lhs left-hand side temp
     * @param rhs right-hand side temp
     * @return a fresh temp as destination
     */
    public Temp visitBinary(TacInstr.Binary.Op op, Temp lhs, Temp rhs) {
        var temp = freshTemp();
        func.add(new TacInstr.Binary(op, temp, lhs, rhs));
        return temp;
    }

    /**
     * Append {@link TacInstr.Binary} of the form {@code self = op self operand}.
     *
     * @param op      binary operator, see {@link TacInstr.Binary.Op}
     * @param self    a temp that is both the destination and the left-hand side
     * @param operand right-hand side temp
     */
    public void visitBinarySelf(TacInstr.Binary.Op op, Temp self, Temp operand) {
        func.add(new TacInstr.Binary(op, self, self, operand));
    }

    /**
     * Append {@link TacInstr.Branch}.
     *
     * @param target label that jumps to
     */
    public void visitBranch(Label target) {
        func.add(new TacInstr.Branch(target));
    }

    /**
     * Append {@link TacInstr.CondBranch}.
     *
     * @param op     when to branch {@link TacInstr.CondBranch.Op}
     * @param cond   condition temp (0 for false, others for true)
     * @param target label that jumps to when {@code cond != 0}
     */
    public void visitBranch(TacInstr.CondBranch.Op op, Temp cond, Label target) {
        func.add(new TacInstr.CondBranch(op, cond, target));
    }

    /**
     * Append {@link TacInstr.Return}, without any return value.
     */
    public void visitReturn() {
        func.add(new TacInstr.Return());
    }

    /**
     * Append {@link TacInstr.Return}, with a return value.
     *
     * @param value return value temp
     */
    public void visitReturn(Temp value) {
        func.add(new TacInstr.Return(value));
    }

    /**
     * Append an instruction to initialize an object/instance.
     *
     * @param clazz class name
     * @return a fresh temp referring to the new object
     */
    public Temp visitNewClass(String clazz) {
        var temp = freshTemp();
        var entry = ctx.getConstructorLabel(clazz);
        func.add(new TacInstr.DirectCall(temp, entry));
        return temp;
    }

    /**
     * Append an instruction to read a member variable.
     *
     * @param object   object ref temp
     * @param clazz    class name
     * @param variable field/member variable name
     * @return a fresh temp as destination
     */
    public Temp visitMemberAccess(Temp object, String clazz, String variable) {
        return visitLoadFrom(object, ctx.getOffset(clazz, variable));
    }

    /**
     * Append an instruction to write a member variable.
     *
     * @param object   object ref temp
     * @param clazz    class name
     * @param variable field/member variable name
     */
    public void visitMemberWrite(Temp object, String clazz, String variable, Temp value) {
        visitStoreTo(object, ctx.getOffset(clazz, variable), value);
    }

    /**
     * Append instructions to invoke a member method.
     *
     * @param object     object ref temp
     * @param clazz      class name
     * @param method     member method name
     * @param args       argument temps
     * @param needReturn do we need a fresh temp to store the return value? (default false)
     * @return the fresh temp if we need return (or else null)
     */
    public Temp visitMemberCall(Temp object, String clazz, String method, List<Temp> args, boolean needReturn) {
        Temp temp = null;
        var vtbl = visitLoadFrom(object);
        var entry = visitLoadFrom(vtbl, ctx.getOffset(clazz, method));

        func.add(new TacInstr.Parm(object));
        for (var arg : args) {
            func.add(new TacInstr.Parm(arg));
        }
        if (needReturn) {
            temp = freshTemp();
            func.add(new TacInstr.IndirectCall(temp, entry));
        } else {
            func.add(new TacInstr.IndirectCall(entry));
        }
        return temp;
    }

    /**
     * @see #visitMemberCall(Temp, String, String, List, boolean)
     */
    public void visitMemberCall(Temp object, String clazz, String method, List<Temp> args) {
        visitMemberCall(object, clazz, method, args, false);
    }

    /**
     * Append instructions to invoke a static method.
     *
     * @param clazz      class name
     * @param method     method name
     * @param args       argument temps
     * @param needReturn do we need a fresh temp to store the return value? (default false)
     * @return the fresh temp if we need return (or else null)
     */
    public Temp visitStaticCall(String clazz, String method, List<Temp> args, boolean needReturn) {
        Temp temp = null;
        var entry = ctx.getFuncLabel(clazz, method);

        for (var arg : args) {
            func.add(new TacInstr.Parm(arg));
        }
        if (needReturn) {
            temp = freshTemp();
            func.add(new TacInstr.DirectCall(temp, entry));
        } else {
            func.add(new TacInstr.DirectCall(entry));
        }
        return temp;
    }

    /**
     * @see #visitStaticCall(String, String, List, boolean)
     */
    public void visitStaticCall(String clazz, String method, List<Temp> args) {
        visitStaticCall(clazz, method, args, false);
    }

    /**
     * Append instructions to invoke an intrinsic method.
     *
     * @param func       intrinsic function
     * @param needReturn do we need a fresh temp to store the return value? (default false)
     * @param args       argument temps
     * @return the fresh temp if we need return (or else null)
     */
    public Temp visitIntrinsicCall(Intrinsic func, boolean needReturn, Temp... args) {
        Temp temp = null;

        for (var arg : args) {
            this.func.add(new TacInstr.Parm(arg));
        }
        if (needReturn) {
            temp = freshTemp();
            this.func.add(new TacInstr.DirectCall(temp, func));
        } else {
            this.func.add(new TacInstr.DirectCall(func));
        }
        return temp;
    }

    /**
     * @see #visitIntrinsicCall(Intrinsic, boolean, Temp...)
     */
    public void visitIntrinsicCall(Intrinsic func, Temp... args) {
        visitIntrinsicCall(func, false, args);
    }

    /**
     * Append an instruction to print a string.
     *
     * @param str string
     */
    public void visitPrint(String str) {
        visitIntrinsicCall(Intrinsic.PRINT_STRING, visitLoad(str));
    }

    /**
     * Append an instruction to load value from memory.
     *
     * @param base   base address temp
     * @param offset offset (default = 0)
     * @return a fresh temp as destination
     */
    public Temp visitLoadFrom(Temp base, int offset) {
        var temp = freshTemp();
        func.add(new TacInstr.Memory(TacInstr.Memory.Op.LOAD, temp, base, offset));
        return temp;
    }

    /**
     * @see #visitLoadFrom(Temp, int)
     */
    public Temp visitLoadFrom(Temp base) {
        return visitLoadFrom(base, 0);
    }

    /**
     * Append an instruction to store value to memory.
     *
     * @param base   base address temp
     * @param offset offset (default = 0)
     * @param value  value temp
     */
    public void visitStoreTo(Temp base, int offset, Temp value) {
        func.add(new TacInstr.Memory(TacInstr.Memory.Op.STORE, value, base, offset));
    }

    /**
     * @see #visitStoreTo(Temp, int, Temp)
     */
    public void visitStoreTo(Temp addr, Temp value) {
        visitStoreTo(addr, 0, value);
    }

    /**
     * Append a label, i.e. {@link TacInstr.Mark}.
     *
     * @param label label
     */
    public void visitLabel(Label label) {
        func.add(new TacInstr.Mark(label));
    }

    /**
     * Append a comment, i.e. {@link TacInstr.Memo}.
     *
     * @param content comment content
     */
    public void visitComment(String content) {
        func.add(new TacInstr.Memo(content));
    }

    /**
     * Append a TAC instruction.
     *
     * @param instr instruction
     */
    public void visitRaw(TacInstr instr) {
        func.add(instr);
    }

    /**
     * Call this when all instructions in this function are done.
     */
    public void visitEnd() {
        // Make sure that every function ends with a return instruction.
        if (func.instrSeq.isEmpty() || !func.instrSeq.get(func.instrSeq.size() - 1).isReturn()) {
            func.add(new TacInstr.Return());
        }
        func.tempUsed = getUsedTemp();
        ctx.funcs.add(func);
    }

    /**
     * Create a fresh temporary label.
     *
     * @return label
     */
    public Label freshLabel() {
        return ctx.freshLabel();
    }

    /**
     * Create a fresh temp.
     *
     * @return temp
     */
    public Temp freshTemp() {
        var temp = new Temp(nextTempId);
        nextTempId++;
        return temp;
    }

    /**
     * Get the temp for the {@code index}-th argument.
     * <p>
     * According to TAC virtual machine calling convention, for a function with {@code n} arguments, the temps with id
     * from 0 to {@code n - 1} are reserved for passing these {@code n} arguments.
     *
     * @param index argument index, start from 0
     * @return temp
     */
    public Temp getArgTemp(int index) {
        return argsTemps[index];
    }

    /**
     * Get total number of used temps so far.
     *
     * @return number of used temps
     */
    public int getUsedTemp() {
        return nextTempId;
    }

    FuncVisitor(FuncLabel entry, int numArgs, ProgramWriter.Context ctx) {
        this.ctx = ctx;
        func = new TacFunc(entry, numArgs);
        visitLabel(entry);
        argsTemps = new Temp[numArgs];
        for (int i = 0; i < numArgs; i++) {
            argsTemps[i] = freshTemp();
        }
    }

    private TacFunc func;

    private ProgramWriter.Context ctx;

    private int nextTempId = 0;

    private Temp[] argsTemps;
}
