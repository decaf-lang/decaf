package decaf.backend.dataflow;

import decaf.lowlevel.instr.PseudoInstr;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Control flow graph.
 * <p>
 * In a control flow graph, the nodes are basic blocks, and an edge {@code (i, j)} indicates that basic block {@code j}
 * is a reachable successor of basic block {@code i}.
 *
 * @param <I> type of the instruction stored in the block
 */
public class CFG<I extends PseudoInstr> implements Iterable<BasicBlock<I>> {

    /**
     * Nodes.
     */
    public final List<BasicBlock<I>> nodes;

    /**
     * Edges.
     */
    public final List<Pair<Integer, Integer>> edges;

    // fst: prev, snd: succ
    private List<Pair<Set<Integer>, Set<Integer>>> links;

    CFG(List<BasicBlock<I>> nodes, List<Pair<Integer, Integer>> edges) {
        this.nodes = nodes;
        this.edges = edges;

        links = new ArrayList<>();
        for (var i = 0; i < nodes.size(); i++) {
            links.add(Pair.of(new TreeSet<>(), new TreeSet<>()));
        }

        for (var edge : edges) {
            var u = edge.getLeft();
            var v = edge.getRight();
            links.get(u).getRight().add(v); // u -> v
            links.get(v).getLeft().add(u); // v <- u
        }
    }

    /**
     * Get basic block by id.
     *
     * @param id basic block id
     * @return basic block
     */
    public BasicBlock<I> getBlock(int id) {
        return nodes.get(id);
    }

    /**
     * Get predecessors.
     *
     * @param id basic block id
     * @return its predecessors
     */
    public Set<Integer> getPrev(int id) {
        return links.get(id).getLeft();
    }

    /**
     * Get successors.
     *
     * @param id basic block id
     * @return its successors
     */
    public Set<Integer> getSucc(int id) {
        return links.get(id).getRight();
    }

    /**
     * Get in-degree.
     *
     * @param id basic block id
     * @return its in-degree
     */
    public int getInDegree(int id) {
        return links.get(id).getLeft().size();
    }

    /**
     * Get out-degree.
     *
     * @param id basic block id
     * @return its out-degree
     */
    public int getOutDegree(int id) {
        return links.get(id).getRight().size();
    }

    @Override
    public Iterator<BasicBlock<I>> iterator() {
        return nodes.iterator();
    }
}
