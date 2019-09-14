package decaf.backend.mips;

import decaf.backend.asm.AsmEmitter;
import decaf.backend.asm.FuncLabel;
import decaf.backend.asm.SubroutineEmitter;
import decaf.backend.asm.SubroutineInfo;
import decaf.instr.Label;
import decaf.instr.PseudoInstr;
import decaf.instr.TacInstr;
import decaf.instr.TodoInstr;
import decaf.instr.tac.StringPool;
import decaf.instr.tac.TAC;
import decaf.utils.MiscUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public final class MipsAsmEmitter extends AsmEmitter {

    public MipsAsmEmitter() {
        super("mips", Mips.allocatableRegs, Mips.callerSaved);

        printer.println("# start of header");
        printer.println(".text");
        printer.println(".globl main");
        printer.println("# end of header");
        printer.println();
    }

    @Override
    public void emitVTable(TAC.VTable vtbl) {
        // vtable begin
        printer.println(".data");
        printer.println(".align 2");

        printer.printLabel(vtbl.label, "virtual table for " + vtbl.className);

        if (vtbl.parent.isPresent()) {
            var index = pool.add(vtbl.parent.get().label.name);
            printer.println(".word %s%d    # parent: %s", STR_PREFIX, index, vtbl.parent.get().className);
        } else {
            printer.println(".word 0    # parent: none");
        }

        var index = pool.add(vtbl.className);
        printer.println(".word %s%d    # class name", STR_PREFIX, index);
        printer.println();
        // vtable end
    }

    @Override
    public Pair<List<PseudoInstr>, SubroutineInfo> selectInstr(TAC.Func func) {
        var selector = new MipsInstrSelector();
        for (var instr : func.getInstrSeq()) {
            instr.accept(selector);
        }

        var info = new SubroutineInfo(new FuncLabel(func.entry.name, func), func.numArgs, selector.hasCall,
                selector.maxArgs * 4);
        return Pair.of(selector.seq, info);
    }

    @Override
    public void emitSubroutineBegin() {
        printer.println(".text");
    }

    @Override
    public SubroutineEmitter emitSubroutine(SubroutineInfo info) {
        return new MipsSubroutineEmitter(this, info);
    }

    @Override
    public String emitEnd() {
        // constant begin
        printer.println(".data");
        var i = 0;
        for (var str : pool) {
            printer.printLabel(new Label(STR_PREFIX + i));
            printer.println(".asciiz %s", MiscUtils.quote(str));
            i++;
        }
        // constant end

        return printer.close();
    }

    private class MipsInstrSelector implements TacInstr.InstrVisitor {

        List<PseudoInstr> seq = new ArrayList<>();

        int maxArgs = 0;

        private int argCount = 0;

        boolean hasCall = false;

        public static final String PREFIX = STR_PREFIX;

        @Override
        public void visitAssign(TacInstr.Assign instr) {
            seq.add(new Mips.Move(instr.dst, instr.src));
        }

        @Override
        public void visitLoadVTbl(TacInstr.LoadVTbl instr) {
            seq.add(new Mips.LoadAddr(instr.dst, instr.vtbl.label));
        }

        @Override
        public void visitLoadImm4(TacInstr.LoadImm4 instr) {
            seq.add(new Mips.LoadImm(instr.dst, instr.value));
        }

        @Override
        public void visitLoadStrConst(TacInstr.LoadStrConst instr) {
            var index = pool.add(instr.value);
            seq.add(new Mips.LoadAddr(instr.dst, new Label(PREFIX + index)));
        }

        @Override
        public void visitUnary(TacInstr.Unary instr) {
            var op = switch (instr.kind) {
                case NEG -> Mips.UnaryOp.NEG;
                case LNOT -> Mips.UnaryOp.NOT;
            };
            seq.add(new Mips.Unary(op, instr.dst, instr.operand));
        }

        @Override
        public void visitBinary(TacInstr.Binary instr) {
            var op = switch (instr.kind) {
                case ADD -> Mips.BinaryOp.ADD;
                case SUB -> Mips.BinaryOp.SUB;
                case MUL -> Mips.BinaryOp.MUL;
                case DIV -> Mips.BinaryOp.DIV;
                case MOD -> Mips.BinaryOp.REM;
                case EQU -> Mips.BinaryOp.SEQ;
                case NEQ -> Mips.BinaryOp.SNE;
                case LES -> Mips.BinaryOp.SLT;
                case LEQ -> Mips.BinaryOp.SLE;
                case GTR -> Mips.BinaryOp.SGT;
                case GEQ -> Mips.BinaryOp.SGE;
                case LAND -> Mips.BinaryOp.AND;
                case LOR -> Mips.BinaryOp.OR;
            };
            seq.add(new Mips.Binary(op, instr.dst, instr.lhs, instr.rhs));
        }

        @Override
        public void visitBranch(TacInstr.Branch instr) {
            seq.add(new Mips.Jump(instr.target));
        }

        @Override
        public void visitCondBranch(TacInstr.CondBranch instr) {
            var op = switch (instr.kind) {
                case BEQZ -> Mips.BranchOp.BEQZ;
                case BNEZ -> Mips.BranchOp.BNEZ;
            };
            seq.add(new Mips.Branch(op, instr.cond, instr.target));
        }

        @Override
        public void visitReturn(TacInstr.Return instr) {
            instr.value.ifPresent(v -> seq.add(new Mips.Move(Mips.V0, v)));
            seq.add(new Mips.JumpToEpilogue());
        }

        @Override
        public void visitParm(TacInstr.Parm instr) {
            if (argCount < 4) {
                seq.add(new Mips.Move(Mips.argRegs[argCount], instr.value));
            } else {
                seq.add(new Mips.StoreWord(instr.value, Mips.SP, argCount * 4));
            }
            argCount++;
        }

        @Override
        public void visitIndirectCall(TacInstr.IndirectCall instr) {
            beforeCall();
            seq.add(new Mips.JumpAndLinkReg(instr.entry));
            afterCall();
            instr.dst.ifPresent(temp -> seq.add(new Mips.Move(temp, Mips.V0)));
        }

        @Override
        public void visitDirectCall(TacInstr.DirectCall instr) {
            beforeCall();
            seq.add(new Mips.JumpAndLink(new Label(instr.entry.name)));
            afterCall();
            instr.dst.ifPresent(temp -> seq.add(new Mips.Move(temp, Mips.V0)));
        }

        private void beforeCall() {
            hasCall = true;
            maxArgs = Math.max(maxArgs, argCount);

            seq.add(TodoInstr.callerSave());
        }

        private void afterCall() {
            seq.add(TodoInstr.callerRestore());
            argCount = 0;
        }

        @Override
        public void visitMemory(TacInstr.Memory instr) {
            seq.add(switch (instr.kind) {
                case LOAD -> new Mips.LoadWord(instr.dst, instr.base, instr.offset);
                case STORE -> new Mips.StoreWord(instr.dst, instr.base, instr.offset);
            });
        }

        @Override
        public void visitMark(TacInstr.Mark instr) {
            seq.add(new Mips.MipsLabel(instr.label));
        }
    }

    private StringPool pool = new StringPool();

    public static final String STR_PREFIX = "_string_";
}
