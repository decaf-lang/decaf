package decaf.tacgen;

import decaf.error.RuntimeError;
import decaf.tools.tac.Intrinsic;
import decaf.tools.tac.MethodVisitor;
import decaf.tools.tac.Tac;
import decaf.tree.Tree;
import decaf.tree.Visitor;
import decaf.type.BuiltInType;

import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TacEmitter extends Visitor<MethodVisitor> {

    Stack<Tac.Label> _loop_exits = new Stack<>();

    @Override
    default void visitBlock(Tree.Block block, MethodVisitor mv) {
        for (var stmt : block.stmts) {
            stmt.accept(this, mv);
        }
    }

    @Override
    default void visitLocalVarDef(Tree.LocalVarDef def, MethodVisitor mv) {
        def.symbol.temp = mv.freshTemp();
    }

    @Override
    default void visitAssign(Tree.Assign assign, MethodVisitor mv) {
        if (assign.lhs instanceof Tree.IndexSel) {
            var indexSel = (Tree.IndexSel) assign.lhs;
            indexSel.array.accept(this, mv);
            indexSel.index.accept(this, mv);
            var addr = emitArrayElementAddress(indexSel.array.val, indexSel.index.val, mv);
            assign.rhs.accept(this, mv);
            mv.visitStoreTo(addr, assign.rhs.val);
        } else if (assign.lhs instanceof Tree.VarSel) {
            var v = (Tree.VarSel) assign.lhs;
            if (v.symbol.isMemberVar()) {
                var object = v.receiver.get();
                object.accept(this, mv);
                assign.rhs.accept(this, mv);
                mv.visitMemberWrite(object.val, v.symbol.getOwner().name, v.name, assign.rhs.val);
            } else { // local or param
                assign.rhs.accept(this, mv);
                mv.visitAssign(v.symbol.temp, assign.rhs.val);
            }
        }
    }

    @Override
    default void visitExprEval(Tree.ExprEval eval, MethodVisitor mv) {
        eval.expr.accept(this, mv);
    }

    @Override
    default void visitIf(Tree.If stmt, MethodVisitor mv) {
        stmt.cond.accept(this, mv);
        Consumer<MethodVisitor> trueBranch = v -> stmt.trueBranch.accept(this, v);

        if (stmt.falseBranch.isEmpty()) {
            emitIfThen(stmt.cond.val, trueBranch, mv);
        } else {
            Consumer<MethodVisitor> falseBranch = v -> stmt.falseBranch.get().accept(this, v);
            emitIfThenElse(stmt.cond.val, trueBranch, falseBranch, mv);
        }
    }

    @Override
    default void visitWhile(Tree.While loop, MethodVisitor mv) {
        var exit = mv.freshLabel();
        Function<MethodVisitor, Tac.Temp> test = v -> {
            loop.cond.accept(this, v);
            return loop.cond.val;
        };
        Consumer<MethodVisitor> body = v -> {
            _loop_exits.push(exit);
            loop.body.accept(this, v);
            _loop_exits.pop();
        };
        emitWhile(test, body, exit, mv);
    }

    @Override
    default void visitFor(Tree.For loop, MethodVisitor mv) {
        var exit = mv.freshLabel();
        loop.init.accept(this, mv);
        Function<MethodVisitor, Tac.Temp> test = v -> {
            loop.cond.accept(this, v);
            return loop.cond.val;
        };
        Consumer<MethodVisitor> body = v -> {
            _loop_exits.push(exit);
            loop.body.accept(this, v);
            _loop_exits.pop();
            loop.update.accept(this, v);
        };
        emitWhile(test, body, exit, mv);
    }

    @Override
    default void visitBreak(Tree.Break stmt, MethodVisitor mv) {
        mv.visitBranch(_loop_exits.peek());
    }

    @Override
    default void visitReturn(Tree.Return stmt, MethodVisitor mv) {
        if (stmt.expr.isEmpty()) {
            mv.visitReturn();
        } else {
            var expr = stmt.expr.get();
            expr.accept(this, mv);
            mv.visitReturn(expr.val);
        }
    }

    @Override
    default void visitPrint(Tree.Print stmt, MethodVisitor mv) {
        for (var expr : stmt.exprs) {
            expr.accept(this, mv);
            if (expr.type.eq(BuiltInType.INT)) {
                mv.visitIntrinsicCall(Intrinsic.PRINT_INT, expr.val);
            } else if (expr.type.eq(BuiltInType.BOOL)) {
                mv.visitIntrinsicCall(Intrinsic.PRINT_BOOL, expr.val);
            } else if (expr.type.eq(BuiltInType.STRING)) {
                mv.visitIntrinsicCall(Intrinsic.PRINT_STRING, expr.val);
            }
        }
    }

    // Expressions

    @Override
    default void visitIntLit(Tree.IntLit expr, MethodVisitor mv) {
        expr.val = mv.visitLoad(expr.value);
    }

    @Override
    default void visitBoolLit(Tree.BoolLit expr, MethodVisitor mv) {
        expr.val = mv.visitLoad(expr.value);
    }

    @Override
    default void visitStringLit(Tree.StringLit expr, MethodVisitor mv) {
        // Remember to unquote the string literal
        var unquoted = expr.value.substring(1, expr.value.length() - 1)
                .replaceAll("\\\\r", "\r")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\t", "\t")
                .replaceAll("\\\\\\\\", "\\")
                .replaceAll("\\\\\"", "\"");
        expr.val = mv.visitLoad(unquoted);
    }

    @Override
    default void visitNullLit(Tree.NullLit expr, MethodVisitor mv) {
        expr.val = mv.visitLoad(0);
    }

    @Override
    default void visitReadInt(Tree.ReadInt expr, MethodVisitor mv) {
        expr.val = mv.visitIntrinsicCall(Intrinsic.READ_INT);
    }

    @Override
    default void visitReadLine(Tree.ReadLine expr, MethodVisitor mv) {
        expr.val = mv.visitIntrinsicCall(Intrinsic.READ_LINE);
    }

    @Override
    default void visitUnary(Tree.Unary expr, MethodVisitor mv) {
        var op = switch (expr.op) {
            case NEG -> Tac.Unary.Op.NEG;
            case NOT -> Tac.Unary.Op.LNOT;
        };

        expr.operand.accept(this, mv);
        expr.val = mv.visitUnary(op, expr.operand.val);
    }

    @Override
    default void visitBinary(Tree.Binary expr, MethodVisitor mv) {
        if ((expr.op.equals(Tree.BinaryOp.EQ) || expr.op.equals(Tree.BinaryOp.NE)) &&
                expr.lhs.type.eq(BuiltInType.STRING)) { // string eq/ne
            expr.lhs.accept(this, mv);
            expr.rhs.accept(this, mv);
            expr.val = mv.visitIntrinsicCall(Intrinsic.STRING_EQUAL, expr.lhs.val, expr.rhs.val);
            if (expr.op.equals(Tree.BinaryOp.NE)) {
                mv.visitUnarySelf(Tac.Unary.Op.LNOT, expr.val);
            }
            return;
        }

        var op = switch (expr.op) {
            case ADD -> Tac.Binary.Op.ADD;
            case SUB -> Tac.Binary.Op.SUB;
            case MUL -> Tac.Binary.Op.MUL;
            case DIV -> Tac.Binary.Op.DIV;
            case MOD -> Tac.Binary.Op.MOD;
            case EQ -> Tac.Binary.Op.EQU;
            case NE -> Tac.Binary.Op.NEQ;
            case LT -> Tac.Binary.Op.LES;
            case LE -> Tac.Binary.Op.LEQ;
            case GT -> Tac.Binary.Op.GTR;
            case GE -> Tac.Binary.Op.GEQ;
            case AND -> Tac.Binary.Op.LAND;
            case OR -> Tac.Binary.Op.LOR;
        };
        expr.lhs.accept(this, mv);
        expr.rhs.accept(this, mv);
        expr.val = mv.visitBinary(op, expr.lhs.val, expr.rhs.val);
    }

    @Override
    default void visitVarSel(Tree.VarSel expr, MethodVisitor mv) {
        if (expr.symbol.isMemberVar()) {
            var object = expr.receiver.get();
            object.accept(this, mv);
            expr.val = mv.visitMemberAccess(object.val, expr.symbol.getOwner().name, expr.name);
        } else { // local or param
            expr.val = expr.symbol.temp;
        }
    }

    @Override
    default void visitIndexSel(Tree.IndexSel expr, MethodVisitor mv) {
        expr.array.accept(this, mv);
        expr.index.accept(this, mv);
        var addr = emitArrayElementAddress(expr.array.val, expr.index.val, mv);
        expr.val = mv.visitLoadFrom(addr);
    }

    @Override
    default void visitNewArray(Tree.NewArray expr, MethodVisitor mv) {
        expr.length.accept(this, mv);
        expr.val = emitArrayInit(expr.length.val, mv);
    }

    @Override
    default void visitNewClass(Tree.NewClass expr, MethodVisitor mv) {
        expr.val = mv.visitNewClass(expr.symbol.name);
    }

    @Override
    default void visitThis(Tree.This expr, MethodVisitor mv) {
        expr.val = mv.getArgTemp(0);
    }

    @Override
    default void visitCall(Tree.Call expr, MethodVisitor mv) {
        if (expr.isArrayLength) { // special case for array.length()
            var array = expr.receiver.get();
            array.accept(this, mv);
            expr.val = mv.visitLoadFrom(array.val, -4);
            return;
        }

        expr.args.forEach(arg -> arg.accept(this, mv));
        var temps = new ArrayList<Tac.Temp>();
        expr.args.forEach(arg -> temps.add(arg.val));

        if (expr.symbol.isStatic()) {
            expr.val = mv.visitStaticCall(expr.symbol.owner.name, expr.symbol.name, temps);
        } else {
            var object = expr.receiver.get();
            object.accept(this, mv);
            expr.val = mv.visitMemberCall(object.val, expr.symbol.owner.name, expr.symbol.name, temps);
        }
    }

    @Override
    default void visitClassTest(Tree.ClassTest expr, MethodVisitor mv) {
        // Accelerate: when obj.type <: class.type, then the test must success!
        if (expr.obj.type.subtypeOf(expr.symbol.type)) {
            expr.val = mv.visitLoad(1);
            return;
        }

        expr.obj.accept(this, mv);
        expr.val = emitClassTest(expr.obj.val, expr.symbol.name, mv);
    }

    @Override
    default void visitClassCast(Tree.ClassCast expr, MethodVisitor mv) {
        expr.obj.accept(this, mv);
        expr.val = expr.obj.val;

        // Accelerate: when obj.type <: class.type, then the test must success!
        if (expr.obj.type.subtypeOf(expr.symbol.type)) {
            return;
        }
        var result = emitClassTest(expr.obj.val, expr.symbol.name, mv);

        /**
         * <pre>
         *     if (result != 0) branch exit  // cast success
         *     print "Decaf runtime error: " // RuntimeError.CLASS_CAST_ERROR1
         *     vtbl1 = *obj                  // vtable of obj
         *     fromClass = *(vtbl1 + 4)      // name of obj's class
         *     print fromClass
         *     print " cannot be cast to "   // RuntimeError.CLASS_CAST_ERROR2
         *     vtbl2 = load vtbl of the target class
         *     toClass = *(vtbl2 + 4)        // name of target class
         *     print toClass
         *     print "\n"                    // RuntimeError.CLASS_CAST_ERROR3
         *     halt
         * exit:
         * </pre>
         */
        var exit = mv.freshLabel();
        mv.visitBranch(Tac.ConditionalBranch.Op.BNEZ, result, exit);
        mv.visitPrint(RuntimeError.CLASS_CAST_ERROR1);
        var vtbl1 = mv.visitLoadFrom(expr.obj.val);
        var fromClass = mv.visitLoadFrom(vtbl1, 4);
        mv.visitIntrinsicCall(Intrinsic.PRINT_STRING, fromClass);
        mv.visitPrint(RuntimeError.CLASS_CAST_ERROR2);
        var vtbl2 = mv.visitLoadVTable(expr.symbol.name);
        var toClass = mv.visitLoadFrom(vtbl2, 4);
        mv.visitIntrinsicCall(Intrinsic.PRINT_STRING, toClass);
        mv.visitPrint(RuntimeError.CLASS_CAST_ERROR3);
        mv.visitIntrinsicCall(Intrinsic.HALT);
        mv.visitLabel(exit);
    }

    /**
     * Emit code for the following conditional statement:
     * <pre>
     *     if (cond) {
     *         action
     *     }
     * </pre>
     *
     * Implementation in pseudo code:
     * <pre>
     *     if (cond == 0) branch skip;
     *     action
     * skip:
     * </pre>
     */
    private void emitIfThen(Tac.Temp cond, Consumer<MethodVisitor> action, MethodVisitor mv) {
        var skip = mv.freshLabel();
        mv.visitBranch(Tac.ConditionalBranch.Op.BEQZ, cond, skip);
        action.accept(mv);
        mv.visitLabel(skip);
    }

    /**
     * Emit code for the following conditional statement:
     * <pre>
     *     if (cond) {
     *         trueBranch
     *     } else {
     *         falseBranch
     *     }
     * </pre>
     *
     * Implementation in pseudo code:
     * <pre>
     *     if (cond == 0) branch skip
     *     trueBranch
     *     branch exit
     * skip:
     *     falseBranch
     * exit:
     * </pre>
     */
    private void emitIfThenElse(Tac.Temp cond, Consumer<MethodVisitor> trueBranch, Consumer<MethodVisitor> falseBranch,
                                MethodVisitor mv) {
        var skip = mv.freshLabel();
        var exit = mv.freshLabel();
        mv.visitBranch(Tac.ConditionalBranch.Op.BEQZ, cond, skip);
        trueBranch.accept(mv);
        mv.visitBranch(exit);
        mv.visitLabel(skip);
        falseBranch.accept(mv);
        mv.visitLabel(exit);
    }

    /**
     * Emit code for the following loop:
     * <pre>
     *     while (cond) {
     *         block
     *     }
     * </pre>
     *
     * Implementation in pseudo code:
     * <pre>
     * entry:
     *     cond = do test
     *     if (cond == 0) branch exit
     *     do block
     *     branch entry
     * exit:
     * </pre>
     */
    private void emitWhile(Function<MethodVisitor, Tac.Temp> test, Consumer<MethodVisitor> block,
                           Tac.Label exit, MethodVisitor mv) {
        var entry = mv.freshLabel();
        mv.visitLabel(entry);
        var cond = test.apply(mv);
        mv.visitBranch(Tac.ConditionalBranch.Op.BEQZ, cond, exit);
        block.accept(mv);
        mv.visitBranch(entry);
        mv.visitLabel(exit);
    }

    /**
     * Emit code for initializing a new array. In memory, an array of length n takes (n + 1) * 4 bytes:
     * - the first 4 bytes: length
     * - the rest bytes: data
     *
     * Pseudo code:
     * <pre>
     *     error = length < 0
     *     if (error) {
     *         throw RuntimeError.NEGATIVE_ARR_SIZE
     *     }
     *
     *     units = length + 1
     *     size = units * 4
     *     a = ALLOCATE(size)
     *     *(a + 0) = length
     *     p = a + size
     *     p -= 4
     *     while (p != a) {
     *         *(p + 0) = 0
     *         p -= 4
     *     }
     *     ret = (a + 4)
     * </pre>
     *
     * @return a temp storing the address of the first element of the array
     */
    private Tac.Temp emitArrayInit(Tac.Temp length, MethodVisitor mv) {
        var zero = mv.visitLoad(0);
        var error = mv.visitBinary(Tac.Binary.Op.LES, length, zero);
        var handler = new Consumer<MethodVisitor>() {
            @Override
            public void accept(MethodVisitor v) {
                v.visitPrint(RuntimeError.NEGATIVE_ARR_SIZE);
                v.visitIntrinsicCall(Intrinsic.HALT);
            }
        };
        emitIfThen(error, handler, mv);

        var units = mv.visitBinary(Tac.Binary.Op.ADD, length, mv.visitLoad(1));
        var four = mv.visitLoad(4);
        var size = mv.visitBinary(Tac.Binary.Op.MUL, units, four);
        var a = mv.visitIntrinsicCall(Intrinsic.ALLOCATE, size);
        mv.visitStoreTo(a, length);
        var p = mv.visitBinary(Tac.Binary.Op.ADD, a, size);
        mv.visitBinarySelf(Tac.Binary.Op.SUB, p, four);
        Function<MethodVisitor, Tac.Temp> test = v -> v.visitBinary(Tac.Binary.Op.NEQ, p, a);
        var body = new Consumer<MethodVisitor>() {
            @Override
            public void accept(MethodVisitor v) {
                v.visitStoreTo(p, zero);
                v.visitBinarySelf(Tac.Binary.Op.SUB, p, four);
            }
        };
        emitWhile(test, body, mv.freshLabel(), mv);
        return mv.visitBinary(Tac.Binary.Op.ADD, a, four);
    }

    /**
     * Emit code for computing the address of an array element.
     *
     * Pseudo code:
     * <pre>
     *     length = *(array - 4)
     *     error1 = index < 0
     *     error2 = index >= length
     *     error = error1 || error2
     *     if (error) {
     *         throw RuntimeError.ARRAY_INDEX_OUT_OF_BOUND
     *     }
     *
     *     offset = index * 4
     *     ret = array + offset
     * </pre>
     *
     * @param array
     * @param index
     * @return a temp storing the address of the element
     */
    private Tac.Temp emitArrayElementAddress(Tac.Temp array, Tac.Temp index, MethodVisitor mv) {
        var length = mv.visitLoadFrom(array, -4);
        var zero = mv.visitLoad(0);
        var error1 = mv.visitBinary(Tac.Binary.Op.LES, index, zero);
        var error2 = mv.visitBinary(Tac.Binary.Op.GEQ, index, length);
        var error = mv.visitBinary(Tac.Binary.Op.LOR, error1, error2);
        var handler = new Consumer<MethodVisitor>() {
            @Override
            public void accept(MethodVisitor v) {
                v.visitPrint(RuntimeError.ARRAY_INDEX_OUT_OF_BOUND);
                v.visitIntrinsicCall(Intrinsic.HALT);
            }
        };
        emitIfThen(error, handler, mv);

        var four = mv.visitLoad(4);
        var offset = mv.visitBinary(Tac.Binary.Op.MUL, index, four);
        return mv.visitBinary(Tac.Binary.Op.ADD, array, offset);
    }

    /**
     * Emit code for testing if an object is an instance of class.
     *
     * Pseudo code:
     * <pre>
     *     target = LoadVtbl(clazz)
     *     t = *object
     * loop:
     *     ret = t == target
     *     if (ret != 0) goto exit
     *     t = *t
     *     if (t != 0) goto loop
     *     ret = 0 // t == null
     * exit:
     * </pre>
     *
     * @param object
     * @param clazz
     * @return
     */
    private Tac.Temp emitClassTest(Tac.Temp object, String clazz, MethodVisitor mv) {
        var target = mv.visitLoadVTable(clazz);
        var t = mv.visitLoadFrom(object);

        var loop = mv.freshLabel();
        var exit = mv.freshLabel();
        mv.visitLabel(loop);
        var ret = mv.visitBinary(Tac.Binary.Op.EQU, t, target);
        mv.visitBranch(Tac.ConditionalBranch.Op.BNEZ, ret, exit);
        mv.visitRaw(new Tac.Memory(Tac.Memory.Op.LOAD, t, t, 0));
        mv.visitBranch(Tac.ConditionalBranch.Op.BNEZ, t, loop);
        var zero = mv.visitLoad(0);
        mv.visitAssign(ret, zero);
        mv.visitLabel(exit);

        return ret;
    }
}
