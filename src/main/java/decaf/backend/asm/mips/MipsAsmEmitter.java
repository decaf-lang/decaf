package decaf.backend.asm.mips;

import decaf.backend.asm.AsmEmitter;
import decaf.backend.asm.HoleInstr;
import decaf.backend.asm.SubroutineEmitter;
import decaf.backend.asm.SubroutineInfo;
import decaf.lowlevel.Mips;
import decaf.lowlevel.StringUtils;
import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.label.IntrinsicLabel;
import decaf.lowlevel.label.Label;
import decaf.lowlevel.tac.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static decaf.lowlevel.Mips.STR_PREFIX;

/**
 * Emit MIPS assembly code.
 */
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
    public void emitVTable(VTable vtbl) {
        // vtable begin
        printer.println(".data");
        printer.println(".align 2");

        printer.printLabel(vtbl.label, "virtual table for " + vtbl.className);

        if (vtbl.parent.isPresent()) {
            var parent = vtbl.parent.get();
            printer.println(".word %s    # parent: %s", parent.label, parent.className);
        } else {
            printer.println(".word 0    # parent: none");
        }

        var index = pool.add(vtbl.className);
        printer.println(".word %s%d    # class name", STR_PREFIX, index);

        for (var entry : vtbl.getItems()) {
            printer.println(".word %s    # member method", entry.name);
        }

        printer.println();
        // vtable end
    }

    @Override
    public Pair<List<PseudoInstr>, SubroutineInfo> selectInstr(TacFunc func) {
        var selector = new MipsInstrSelector(func.entry);
        for (var instr : func.getInstrSeq()) {
            instr.accept(selector);
        }

        var info = new SubroutineInfo(func.entry, func.numArgs, selector.hasCall, selector.maxArgs * 4);
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
        if (!usedIntrinsics.isEmpty()) {
            printer.println("# start of intrinsics");
            if (usedIntrinsics.contains(Intrinsic.READ_LINE.entry)) {
                loadReadLine();
            }
            if (usedIntrinsics.contains(Intrinsic.STRING_EQUAL.entry)) {
                loadStringEqual();
            }
            if (usedIntrinsics.contains(Intrinsic.PRINT_BOOL.entry)) {
                loadPrintBool();
            }
            printer.println("# end of intrinsics");
            printer.println();
        }

        printer.println("# start of constant strings");
        printer.println(".data");
        var i = 0;
        for (var str : pool) {
            printer.printLabel(new Label(STR_PREFIX + i));
            printer.println(".asciiz %s", StringUtils.quote(str));
            i++;
        }
        printer.println("# end of constant strings");

        return printer.close();
    }

    private void loadReadLine() {
        var loop = new Label(Intrinsic.READ_LINE.entry + "_loop");
        var exit = new Label(Intrinsic.READ_LINE.entry + "_exit");

        printer.printLabel(Intrinsic.READ_LINE.entry, "intrinsic: read line");
        printer.println("sw $a0, -4($sp)");
        printer.println("sw $a1, -8($sp)");
        printer.println("li $a0, 64    # allocate space, fixed size 64");
        printer.println("li $v0, 9     # memory allocation");
        printer.println("syscall");
        printer.println("move $a0, $v0");
        printer.println("li $a1, 64");
        printer.println("li $v0, 8     # read string");
        printer.println("syscall");
        printer.println("move $v0, $a0");
        printer.printLabel(loop);
        printer.println("lb $a1, ($a0)");
        printer.println("beqz $a1, %s", exit);
        printer.println("addi $a1, $a1, -10  # subtract ASCII newline");
        printer.println("beqz $a1, %s", exit);
        printer.println("addi $a0, $a0, 1");
        printer.println("j %s", loop);
        printer.printLabel(exit);
        printer.println("sb $a1, ($a0)");
        printer.println("lw $a0, -4($sp)");
        printer.println("lw $a1, -8($sp)");
        printer.println("jr $ra");
        printer.println();
    }

    private void loadStringEqual() {
        var loop = new Label(Intrinsic.STRING_EQUAL.entry + "_loop");
        var exit = new Label(Intrinsic.STRING_EQUAL.entry + "_exit");

        printer.printLabel(Intrinsic.STRING_EQUAL.entry, "intrinsic: string equal");
        printer.println("sw $a2, -4($sp)");
        printer.println("sw $a3, -8($sp)");
        printer.println("li $v0, 1");
        printer.printLabel(loop);
        printer.println("lb $a2, ($a0)");
        printer.println("lb $a3, ($a1)");
        printer.println("seq $v0, $a2, $a3");
        printer.println("beqz $v0, %s", exit);
        printer.println("beqz $a2, %s", exit);
        printer.println("addiu $a0, $a0, 1");
        printer.println("addiu $a1, $a1, 1");
        printer.println("j %s", loop);
        printer.printLabel(exit);
        printer.println("lw $a2, -4($sp)");
        printer.println("lw $a3, -8($sp)");
        printer.println("jr $ra");
        printer.println();
    }

    private void loadPrintBool() {
        var trueString = new Label(Intrinsic.PRINT_BOOL.entry + "_S_true");
        var falseString = new Label(Intrinsic.PRINT_BOOL.entry + "_S_false");
        var isFalse = new Label(Intrinsic.PRINT_BOOL.entry + "_false");

        printer.printLabel(Intrinsic.PRINT_BOOL.entry, "intrinsic: print bool");
        printer.println(".data");
        printer.printLabel(trueString);
        printer.println(".asciiz \"true\"");
        printer.printLabel(falseString);
        printer.println(".asciiz \"false\"");

        printer.println(".text");
        printer.println("li $v0, 4    # print string");
        printer.println("beqz $a0, %s", isFalse);
        printer.println("la $a0, %s", trueString);
        printer.println("syscall");
        printer.println("jr $ra");
        printer.printLabel(isFalse);
        printer.println("la $a0, %s", falseString);
        printer.println("syscall");
        printer.println("jr $ra");
    }

    private class MipsInstrSelector implements TacInstr.Visitor {

        MipsInstrSelector(Label entry) {
            this.entry = entry;
        }

        List<PseudoInstr> seq = new ArrayList<>();

        Label entry;

        int maxArgs = 0;

        private int argCount = 0;

        boolean hasCall = false;

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
            seq.add(new Mips.LoadAddr(instr.dst, new Label(STR_PREFIX + index)));
        }

        @Override
        public void visitUnary(TacInstr.Unary instr) {
            var op = switch (instr.op) {
                case NEG -> Mips.UnaryOp.NEG;
                case LNOT -> Mips.UnaryOp.NOT;
            };
            seq.add(new Mips.Unary(op, instr.dst, instr.operand));
        }

        @Override
        public void visitBinary(TacInstr.Binary instr) {
            var op = switch (instr.op) {
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
            var op = switch (instr.op) {
                case BEQZ -> Mips.BranchOp.BEQZ;
                case BNEZ -> Mips.BranchOp.BNEZ;
            };
            seq.add(new Mips.Branch(op, instr.cond, instr.target));
        }

        @Override
        public void visitReturn(TacInstr.Return instr) {
            instr.value.ifPresent(v -> seq.add(new Mips.Move(Mips.V0, v)));
            seq.add(new Mips.JumpToEpilogue(entry));
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
            hasCall = true;

            callerSave();
            seq.add(new Mips.JumpAndLinkReg(instr.entry));
            callerRestore();

            argCount = 0;

            instr.dst.ifPresent(temp -> seq.add(new Mips.Move(temp, Mips.V0)));
        }

        @Override
        public void visitDirectCall(TacInstr.DirectCall instr) {
            hasCall = true;

            if (instr.entry.isIntrinsic()) { // special case: inline or embed the code (no registers need be saved)
                var il = (IntrinsicLabel) instr.entry;
                switch (il.opcode) {
                    case ALLOCATE -> {
                        seq.add(new Mips.LoadImm(Mips.V0, 9)); // memory allocation
                        seq.add(new Mips.Syscall());
                    }
                    case READ_INT -> {
                        seq.add(new Mips.LoadImm(Mips.V0, 5)); // read integer
                        seq.add(new Mips.Syscall());
                    }
                    case PRINT_INT -> {
                        seq.add(new Mips.LoadImm(Mips.V0, 1)); // print integer
                        seq.add(new Mips.Syscall());
                    }
                    case PRINT_STRING -> {
                        seq.add(new Mips.LoadImm(Mips.V0, 4)); // print string
                        seq.add(new Mips.Syscall());
                    }
                    case HALT -> {
                        seq.add(new Mips.LoadImm(Mips.V0, 10)); // exit
                        seq.add(new Mips.Syscall());
                    }
                    default -> {
                        seq.add(new Mips.JumpAndLink(il));
                        usedIntrinsics.add(il);
                    }
                }
            } else {  // normal call
                callerSave();
                seq.add(new Mips.JumpAndLink(new Label(instr.entry.name)));
                callerRestore();
            }

            argCount = 0;

            // finally
            instr.dst.ifPresent(temp -> seq.add(new Mips.Move(temp, Mips.V0)));
        }

        private void callerSave() {
            maxArgs = Math.max(maxArgs, argCount);
            seq.add(HoleInstr.CallerSave);
        }

        private void callerRestore() {
            seq.add(HoleInstr.CallerRestore);
        }

        @Override
        public void visitMemory(TacInstr.Memory instr) {
            seq.add(switch (instr.op) {
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

    private Set<IntrinsicLabel> usedIntrinsics = new TreeSet<>();
}
