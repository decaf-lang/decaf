package decaf.backend.reg;

import decaf.backend.asm.AsmEmitter;
import decaf.backend.asm.HoleInstr;
import decaf.backend.asm.SubroutineEmitter;
import decaf.backend.asm.SubroutineInfo;
import decaf.backend.dataflow.BasicBlock;
import decaf.backend.dataflow.CFG;
import decaf.backend.dataflow.Loc;
import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.instr.Reg;
import decaf.lowlevel.instr.Temp;

import java.util.*;

/**
 * Brute force greedy register allocation algorithm.
 * <p>
 * To make our life easier, don't consider any special registers that may be used during call.
 */
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
     * <ul>
     *     <li>Allocation is preformed block-by-block.</li>
     *     <li>Assume that every allocatable register is unoccupied before entering every basic block.</li>
     *     <li>For every read (src) and written (dst) temp {@code t} in every pseudo instruction, attempt the following
     *     in order:</li>
     *     <li><ol>
     *         <li>{@code t} is already bound to a register: keep on using it.</li>
     *         <li>If there exists an available (unoccupied, or the occupied temp is no longer alive) register,
     *         then bind to it.</li>
     *         <li>Arbitrarily pick a general register, spill its value to stack, and then bind to it.</li>
     *     </ol></li>
     * </ul>
     * <p>
     * The output assembly code is maintained by {@code emitter}.
     *
     * @param bb         the basic block which the algorithm performs on
     * @param subEmitter the current subroutine emitter
     * @see #allocRegFor
     */
    private void localAlloc(BasicBlock<PseudoInstr> bb, SubroutineEmitter subEmitter) {
        bindings.clear();
        for (var reg : emitter.allocatableRegs) {
            reg.occupied = false;
        }

        var callerNeedSave = new ArrayList<Reg>();

        for (var loc : bb.allSeq()) {
            // Handle special instructions on caller save/restore.

            if (loc.instr instanceof HoleInstr) {
                if (loc.instr.equals(HoleInstr.CallerSave)) {
                    for (var reg : emitter.callerSaveRegs) {
                        if (reg.occupied && loc.liveOut.contains(reg.temp)) {
                            callerNeedSave.add(reg);
                            subEmitter.emitStoreToStack(reg);
                        }
                    }
                    continue;
                }

                if (loc.instr.equals(HoleInstr.CallerRestore)) {
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
        if (!bb.isEmpty() && !bb.kind.equals(BasicBlock.Kind.CONTINUOUS)) {
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
     * @param temp       temp appeared in the pseudo instruction
     * @param isRead     true = read, false = write
     * @param live       set of live temps before executing this instruction
     * @param subEmitter current subroutine emitter
     * @return register for use
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

    /**
     * Random number generator.
     */
    private Random random = new Random();
}
