package decaf.backend.dataflow;

import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.instr.Temp;
import decaf.lowlevel.label.Label;

import java.util.*;

/**
 * A basic block in a control flow graph.
 *
 * @param <I> type of the instructions stored in the block
 */
public class BasicBlock<I extends PseudoInstr> implements Iterable<Loc<I>> {
    /**
     * Kind of the block.
     * <ol>
     *     <li>{@code CONTINUOUS}: continue executing the next block after this one</li>
     *     <li>{@code END_BY_JUMP}: this block ends by a jump instruction</li>
     *     <li>{@code END_BY_COND_JUMP}: this block ends by a conditional jump instruction</li>
     *     <li>{@code END_BY_RETURN}: this block ends by a return instruction</li>
     * </ol>
     */
    public enum Kind {
        CONTINUOUS, END_BY_JUMP, END_BY_COND_JUMP, END_BY_RETURN
    }

    public final Kind kind;

    /**
     * Block id.
     */
    public final int id;

    /**
     * Entry label of this block, if any.
     */
    public final Optional<Label> label;

    /**
     * List of locations (i.e. instructions with liveness info).
     */
    public final List<Loc<I>> locs;

    public BasicBlock(Kind kind, int id, Optional<Label> label, List<Loc<I>> locs) {
        this.kind = kind;
        this.id = id;
        this.label = label;
        this.locs = new ArrayList<>(locs);
    }

    public boolean isEmpty() {
        return locs.isEmpty();
    }

    @Override
    public Iterator<Loc<I>> iterator() {
        return locs.iterator();
    }

    public Iterator<Loc<I>> backwardIterator() {
        return new Iterator<>() {
            private int i = locs.size() - 1;

            @Override
            public boolean hasNext() {
                return i != -1;
            }

            @Override
            public Loc<I> next() {
                var loc = locs.get(i);
                i--;
                return loc;
            }
        };
    }

    /**
     * Get all sequential instructions. The last instruction will be included iff this block is {@link Kind#CONTINUOUS}.
     *
     * @return sequential instructions
     */
    public List<Loc<I>> allSeq() {
        if (kind.equals(Kind.CONTINUOUS)) {
            return locs;
        }

        return locs.subList(0, locs.size() - 1);
    }

    /**
     * Get the last instruction.
     *
     * @return the last instruction
     */
    public I getLastInstr() {
        return locs.get(locs.size() - 1).instr;
    }

    // For data flow analysis
    public Set<Temp> def;

    public Set<Temp> liveUse;

    public Set<Temp> liveIn;

    public Set<Temp> liveOut;
}
