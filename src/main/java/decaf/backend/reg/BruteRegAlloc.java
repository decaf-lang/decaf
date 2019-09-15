package decaf.backend.reg;

import decaf.backend.asm.AsmEmitter;
import decaf.backend.asm.SubroutineEmitter;
import decaf.backend.asm.SubroutineInfo;
import decaf.dataflow.BasicBlock;
import decaf.dataflow.CFG;
import decaf.dataflow.Loc;
import decaf.instr.PseudoInstr;
import decaf.instr.Reg;
import decaf.instr.Temp;
import decaf.instr.TodoInstr;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// Brute force greedy register allocator.
// To make our life easier, don't consider any special registers that may be used during call.
public final class BruteRegAlloc extends RegAlloc {

    public BruteRegAlloc(AsmEmitter emitter) {
        super(emitter);
        for (var reg : emitter.allocatableRegs) {
            reg.used = false;
        }
    }

    @Override
    public void accept(CFG<PseudoInstr> graph, SubroutineInfo info) {
        var subEmitter = emitter.emitSubroutine(info);
        for (var bb : graph) {
            bb.label.ifPresent(subEmitter::emitLabel);
            localAlloc(bb, subEmitter);
        }
        subEmitter.emitEnd();
    }

    private Map<Temp, Reg> bindings = new TreeMap<>();

    private void bind(Temp temp, Reg reg) {
        reg.used = true;

        bindings.put(temp, reg);
        reg.occupied = true;
        reg.temp = temp;
    }

    private void unbind(Temp temp) {
        if (bindings.containsKey(temp)) {
            bindings.get(temp).occupied = false;
            bindings.remove(temp);
        }
    }

    /**
     * Main algorithm of local register allocation à la brute-force. Basic idea:
     * - Allocation is preformed block-by-block.
     * - Assume that every allocatable unlocked register is unoccupied before entering every basic block.
     * - For every read (src) & written (dst) temp `t` in every pseudo instruction, attempt the following in order:
     * 1. `t` is already bound to a register: keep on using it.
     * 2. If there exists an available (unoccupied, or the occupied temp is no longer alive) register: bind to it.
     * 3. Arbitrarily pick a general register (avoid being locked), spill its value to stack, and then bind to it.
     *
     * {@see allocRegFor} for more details.
     *
     * The output assembly code is stored by the {@code emitter}.
     *
     * @param bb         the basic block which the algorithm performs on
     * @param subEmitter the current subroutine emitter
     */
    private void localAlloc(BasicBlock<PseudoInstr> bb, SubroutineEmitter subEmitter) {
        bindings.clear();
        for (var reg : emitter.allocatableRegs) {
            reg.occupied = false;
        }

        var callerNeedSave = new ArrayList<Reg>();

        for (var loc : bb.seqLocs()) {
            // Handle special instructions on caller save/restore.

            if (loc.instr.isTodo()) {
                var todo = (TodoInstr) loc.instr;

                if (todo.isCallerSave()) {
                    for (var reg : emitter.callerSaveRegs) {
                        if (reg.occupied && loc.liveOut.contains(reg.temp)) {
                            callerNeedSave.add(reg);
                            subEmitter.emitStoreToStack(reg);
                        }
                    }
                    continue;
                }

                if (todo.isCallerRestore()) {
                    for (var reg : callerNeedSave) {
                        subEmitter.emitLoadFromStack(reg, reg.temp);
                    }
                    callerNeedSave.clear();
                    continue;
                }
            }

            // For normal instructions: allocate registers for every read/written temp. Skip the already specified
            // special registers.
            allocForLoc(loc, subEmitter);
        }

        // Before we leave a basic block, we must copy values of all live variables from registers (if exist)
        // to stack, as all these registers will be reset (as unoccupied) when entering another basic block.
        for (var temp : bb.liveOut) {
            if (bindings.containsKey(temp)) {
                subEmitter.emitStoreToStack(bindings.get(temp));
            }
        }

        // Handle the last instruction, if it is a branch/return block.
        if (!bb.isEmpty() && !bb.kind.equals(BasicBlock.Kind.CONTINUE)) {
            allocForLoc(bb.locs.get(bb.locs.size() - 1), subEmitter);
        }
    }

    private void allocForLoc(Loc<PseudoInstr> loc, SubroutineEmitter subEmitter) {
        var instr = loc.instr;
        var srcRegs = new Reg[instr.srcs.length];
        var dstRegs = new Reg[instr.dsts.length];

        for (var i = 0; i < instr.srcs.length; i++) {
            var temp = instr.srcs[i];
            if (temp instanceof Reg) {
                srcRegs[i] = (Reg) temp;
            } else {
                srcRegs[i] = allocRegFor(temp, true, loc.liveIn, subEmitter);
            }
        }

        for (var i = 0; i < instr.dsts.length; i++) {
            var temp = instr.dsts[i];
            if (temp instanceof Reg) {
                dstRegs[i] = ((Reg) temp);
            } else {
                dstRegs[i] = allocRegFor(temp, false, loc.liveIn, subEmitter);
            }
        }

        subEmitter.emitNative(instr.toNative(dstRegs, srcRegs));
    }

    /**
     * Allocate a register for a temp.
     *
     * @param temp       the temp appeared in the pseudo instruction
     * @param isRead     true = read, false = write
     * @param live       the set of live temps before executing this instruction
     * @param subEmitter the current subroutine emitter
     * @return the allocated register
     */
    private Reg allocRegFor(Temp temp, boolean isRead, Set<Temp> live, SubroutineEmitter subEmitter) {
        // Best case: the value of `temp` is already in register.
        if (bindings.containsKey(temp)) {
            return bindings.get(temp);
        }

        // First attempt: find an unoccupied register, or one whose value is no longer alive at this location.
        for (var reg : emitter.allocatableRegs) {
            if (!reg.occupied || !live.contains(reg.temp)) {
                if (isRead) {
                    // Since `reg` is uninitialized, we must load the latest value of `temp`, from stack, to `reg`.
                    subEmitter.emitLoadFromStack(reg, temp);
                }
                if (reg.occupied) {
                    unbind(reg.temp);
                }
                bind(temp, reg);
                return reg;
            }
        }

        // Last attempt: all registers are occupied, so we have to spill one of them.
        // To avoid the situation where the first register is consecutively spilled, a reasonable heuristic
        // is to randomize our choice among all of them.
        var reg = emitter.allocatableRegs[random.nextInt(emitter.allocatableRegs.length)];
        subEmitter.emitStoreToStack(reg);
        unbind(reg.temp);
        bind(temp, reg);
        if (isRead) {
            subEmitter.emitLoadFromStack(reg, temp);
        }
        return reg;
    }
}
