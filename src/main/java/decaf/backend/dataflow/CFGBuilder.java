package decaf.backend.dataflow;

import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.label.Label;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Control flow graph builder.
 *
 * @param <I> type of instructions
 */
public class CFGBuilder<I extends PseudoInstr> {
    /**
     * Build a control flow graph from a sequence of instructions.
     *
     * @param seq instruction sequence
     * @return corresponding control flow graph
     */
    public CFG<I> buildFrom(List<I> seq) {
        for (var item : seq) {
            if (item.isLabel()) {
                if (item.label.isFunc()) {
                    // ignore function label
                } else {
                    // close the previous basic block
                    close();
                    // associate the current basic block with this label
                    currentBBLabel = Optional.of(item.label);
                }
            } else {
                buf.add(new Loc<>(item));
                if (!item.isSequential()) { // branching, finish this basic block
                    var kind = switch (item.kind) {
                        case JMP -> BasicBlock.Kind.END_BY_JUMP;
                        case COND_JMP -> BasicBlock.Kind.END_BY_COND_JUMP;
                        case RET -> BasicBlock.Kind.END_BY_RETURN;
                        default -> null;
                    };
                    var bb = new BasicBlock<>(kind, bbs.size(), currentBBLabel, buf);
                    save(bb);
                }
            }
        }
        if (!buf.isEmpty()) {
            throw new IllegalArgumentException("encounter a non-returned basic block");
        }

        var edges = new ArrayList<Pair<Integer, Integer>>();
        var iter = bbs.iterator();
        while (iter.hasNext()) {
            var bb = iter.next();
            switch (bb.kind) {
                case END_BY_JUMP -> {
                    // can only continue to execute the block we wish to jump into
                    Objects.requireNonNull(labelsToBBs.get(bb.getLastInstr().label));
                    edges.add(Pair.of(bb.id, labelsToBBs.get(bb.getLastInstr().label)));
                }
                case END_BY_COND_JUMP -> {
                    // can continue to execute either the block we wish to jump into, or the next block (if exists)
                    Objects.requireNonNull(labelsToBBs.get(bb.getLastInstr().label));
                    edges.add(Pair.of(bb.id, labelsToBBs.get(bb.getLastInstr().label)));
                    if (iter.hasNext()) {
                        edges.add(Pair.of(bb.id, bb.id + 1));
                    }
                }
                case END_BY_RETURN -> {
                    // stop
                }
                default -> {
                    // can only continue to execute the next block
                    if (iter.hasNext()) {
                        edges.add(Pair.of(bb.id, bb.id + 1));
                    }
                }
            }
        }

        return new CFG<>(bbs, edges);
    }

    private List<BasicBlock<I>> bbs = new ArrayList<>();

    private List<Loc<I>> buf = new ArrayList<>();

    private Optional<Label> currentBBLabel = Optional.empty();

    private Map<Label, Integer> labelsToBBs = new TreeMap<>();

    private void save(BasicBlock<I> bb) {
        bbs.add(bb);
        buf.clear();
        currentBBLabel = Optional.empty();

        bb.label.ifPresent(lbl -> labelsToBBs.put(lbl, bb.id));
    }

    private void close() {
        var bb = new BasicBlock<>(BasicBlock.Kind.CONTINUOUS, bbs.size(), currentBBLabel, buf);
        save(bb);
    }
}
