package decaf.backend.asm.x86;

import decaf.backend.asm.SubroutineEmitter;
import decaf.backend.asm.SubroutineInfo;

import static decaf.lowlevel.X86.*;

import decaf.lowlevel.instr.NativeInstr;
import decaf.lowlevel.instr.Reg;
import decaf.lowlevel.instr.Temp;
import decaf.lowlevel.label.Label;
import decaf.lowlevel.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class X86SubroutineEmitter extends SubroutineEmitter {

    X86SubroutineEmitter(X86AsmEmitter emitter, SubroutineInfo info) {
        super(emitter, info);
        // assume all registers may need callee-saving.
        lastLocalOffset = - 4 * calleeSaved.length;
        printer.printLabel(info.funcLabel, "function " + info.funcLabel.prettyString());
    }

    @Override
    public void emitComment(String comment) {
        buf.add(NativeInstr.nativeComment(comment));
    }

    @Override
    public void emitStoreToStack(Reg src) {
        if (!offsets.containsKey(src.temp)) {
            if (src.temp.index < info.numArg) {
                offsets.put(src.temp, 8 + 4 * src.temp.index);
            } else {
                lastLocalOffset -= 4;
                offsets.put(src.temp, lastLocalOffset);
            }
        }

        buf.add(new NativeStoreWord(src, EBP, offsets.get(src.temp)));
    }

    @Override
    public void emitLoadFromStack(Reg dst, Temp src) {
        if (!offsets.containsKey(src)) {
            if (src.index < info.numArg) {
                var offset = 8 + 4 * src.index;
                offsets.put(src, offset);
            } else {
                throw new IllegalArgumentException("offsets doesn't contain " + src + " when loading " + dst);
            }
        }

        buf.add(new NativeLoadWord(dst, EBP, offsets.get(src)));
    }

    @Override
    public void emitMove(Reg dst, Reg src) {
        if (dst != src)
            buf.add(new NativeMove(dst, src));
    }

    @Override
    public void emitNative(NativeInstr instr) {
        buf.add(instr);
    }

    @Override
    public void emitLabel(Label label) {
        buf.add(new X86Label(label).toNative(new Reg[]{}, new Reg[]{}));
    }

    private void calleeSave() {
        for (var i = 0; i < calleeSaved.length; i++) {
            if (calleeSaved[i].isUsed()) {
                var instr = new NativeStoreWord(calleeSaved[i], EBP, - 4 * (i+1));
                printer.printInstr(instr, "save value of $S" + i);
            }
        }
    }

    private void calleeRestore() {
        for (var i = 0; i < calleeSaved.length; i++) {
            if (calleeSaved[i].isUsed()) {
                printer.printInstr(new NativeLoadWord(calleeSaved[i], EBP, - 4 * (i+1)),
                        "restore value of $S" + i);
            }
        }
    }

    private void emitPrologue() {
        printer.printComment("start of prologue");
        printer.printInstr(new NativePush(EBP));
        printer.printInstr(new NativeMove(EBP, ESP));
        printer.printInstr(new RSPAdd(lastLocalOffset), "push stack frame");
        calleeSave();
        printer.printComment("end of prologue");
        printer.println();
    }

    private void emitEpilogue() {
        printer.printLabel(new Label(info.funcLabel.name + EPILOGUE_SUFFIX));
        printer.printComment("start of epilogue");
        calleeRestore();
        printer.printInstr(new NativeLeave());
        printer.printInstr(new NativeReturn());
        printer.printComment("end of epilogue");
        printer.println();
    }

    private void emitBody() {
        printer.printComment("start of body");
        for (var instr : buf) {
            if (instr instanceof CopyCCNative) {
                var instr1 = (CopyCCNative) instr;
                if (instr1.dsts[0] == EAX) {
                    printer.printInstr(new NativeSetCC(instr1.op, EAX));
                    printer.printInstr(new ExtendFrom8Bit(ExtendOp.MOVZBL, EAX, EAX));
                } else {
                    printer.printInstr(new NativePush(EAX));
                    printer.printInstr(new NativeSetCC(instr1.op, EAX));
                    printer.printInstr(new ExtendFrom8Bit(ExtendOp.MOVZBL, (Reg) instr1.dsts[0], EAX));
                    printer.printInstr(new NativePop(EAX));
                }
                continue;
            }
            if (instr instanceof CondJumpNative) {
                var instr1 = (CondJumpNative) instr;
                printer.printInstr(new NativeCompareToZero((Reg) instr1.srcs[0]));
                printer.printInstr(instr1);
                continue;
            }
            if (instr instanceof SignedIntDivRemNative) {
                var instr1 = (SignedIntDivRemNative) instr;
                printer.printInstr(new NativePush(EAX));
                printer.printInstr(new NativePush(EDX));
                printer.printInstr(new NativeMove(EAX, (Reg) instr1.srcs[0]));
                printer.printInstr(new NativeCLTD());
                printer.printInstr(new NativeDivRem((Reg) instr1.srcs[1]));
                printer.printInstr(new NativeMove((Reg) instr1.dsts[0],
                        (instr1.op == SignedIntDivRemOp.DIV) ? EAX : EDX));
                printer.printInstr(new NativePop(EDX));
                printer.printInstr(new NativePop(EAX));
                continue;
            }

            printer.printInstr(instr);
        }
        printer.printComment("end of body");
        printer.println();
    }

    @Override
    public void emitEnd() {
        Log.info("Function %s", info.funcLabel.name);
        offsets.forEach((k, v) -> {
            Log.info("Temp %4s Offset %4d", k, v);
        });
        // Called after register allocation has been done.
        emitPrologue();
        emitBody();
        emitEpilogue();
    }

    private List<NativeInstr> buf = new ArrayList<>();

    private int lastLocalOffset = 0;

    // offsets from ebp.
    // argument offset >= 8. callee-save and locals offset < 0.
    private Map<Temp, Integer> offsets = new TreeMap<>();
}
