package decaf.dataflow;

import decaf.instr.InstrLike;
import decaf.instr.Label;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class CFGBuilder<I extends InstrLike> {

//    public CFGBuilder(boolean simplify) {
//        this.simplify = simplify;
//    }

    public CFGBuilder() {
        simplify = false;
    }

    public CFG<I> buildFrom(List<I> seq) {
        for (var item : seq) {
            if (item.isLabel()) {
                if (item.jumpTo.name.equals("main") || item.jumpTo.name.startsWith("_L_")) {
                    // ignore
                } else {
                    // close the previous basic block
                    close();
                    // associate the current basic block with this label
                    currentBBLabel = Optional.of(item.jumpTo);
                }
            } else {
                buf.add(new Loc<>(item));
                if (!item.isSequential()) { // branching, finish this basic block
                    var kind = switch (item.kind) {
                        case JMP -> BasicBlock.Kind.BY_BRANCH;
                        case COND_JMP -> BasicBlock.Kind.BY_COND_BRANCH;
                        case RET -> BasicBlock.Kind.BY_RETURN;
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
                case BY_BRANCH -> {
                    // can only continue to execute the block we wish to jump into
                    Objects.requireNonNull(labelsToBBs.get(bb.getLastInstr().jumpTo));
                    edges.add(Pair.of(bb.bbNum, labelsToBBs.get(bb.getLastInstr().jumpTo)));
                }
                case BY_COND_BRANCH -> {
                    // can continue to execute either the block we wish to jump into, or the next block (if exists)
                    Objects.requireNonNull(labelsToBBs.get(bb.getLastInstr().jumpTo));
                    edges.add(Pair.of(bb.bbNum, labelsToBBs.get(bb.getLastInstr().jumpTo)));
                    if (iter.hasNext()) {
                        edges.add(Pair.of(bb.bbNum, bb.bbNum + 1));
                    }
                }
                case BY_RETURN -> {
                    // stop
                }
                default -> {
                    // can only continue to execute the next block
                    if (iter.hasNext()) {
                        edges.add(Pair.of(bb.bbNum, bb.bbNum + 1));
                    }
                }
            }
        }

//        if (simplify) { // eliminate unreachable basic blocks TODO
//
//        }

        return new CFG<>(bbs, edges);
    }

    private boolean simplify = false;

    private List<BasicBlock<I>> bbs = new ArrayList<>();

    private List<Loc<I>> buf = new ArrayList<>();

    private Optional<Label> currentBBLabel = Optional.empty();

    private Map<Label, Integer> labelsToBBs = new TreeMap<>();

    private void save(BasicBlock<I> bb) {
//        System.out.println("Buiding:");
//        buf.forEach(System.out::println);

        bbs.add(bb);
        buf.clear();
        currentBBLabel = Optional.empty();

        bb.label.ifPresent(lbl -> labelsToBBs.put(lbl, bb.bbNum));
    }

    private void close() {
//        System.out.println("Buiding:");
//        buf.forEach(System.out::println);

//        if (!buf.isEmpty()) {
        var bb = new BasicBlock<>(BasicBlock.Kind.CONTINUE, bbs.size(), currentBBLabel, buf);
        save(bb);
//        }
    }
}
