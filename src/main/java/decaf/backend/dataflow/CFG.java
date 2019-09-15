package decaf.backend.dataflow;

import decaf.lowlevel.InstrLike;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintWriter;
import java.util.*;

public class CFG<I extends InstrLike> implements Iterable<BasicBlock<I>> {

    public final List<BasicBlock<I>> nodes;

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

    public Set<Integer> getPrev(int id) {
        return links.get(id).getLeft();
    }

    public Set<Integer> getSucc(int id) {
        return links.get(id).getRight();
    }

    public int getInDegree(int id) {
        return links.get(id).getLeft().size();
    }

    public int getOutDegree(int id) {
        return links.get(id).getRight().size();
    }

    @Override
    public Iterator<BasicBlock<I>> iterator() {
        return nodes.iterator();
    }

    public BasicBlock<I> getBlock(int i) {
        return nodes.get(i);
    }

    public int size() {
        return nodes.size();
    }

    public void printTo(PrintWriter pw) {
        pw.println("CFG : ");
        for (BasicBlock bb : nodes) {
            bb.printTo(pw);
            pw.print(bb.kind);
            pw.print(", succ");
            for (var b : getSucc(bb.bbNum)) {
                pw.print(' ');
                pw.print(b);
            }
            pw.println();
        }
    }

    public void printLivenessTo(PrintWriter pw) {
        pw.println("CFG : ");
        for (BasicBlock bb : nodes) {
            bb.printLivenessTo(pw);
            pw.print(bb.kind);
            pw.print(", succ");
            for (var b : getSucc(bb.bbNum)) {
                pw.print(' ');
                pw.print(b);
            }
            pw.println();
        }
    }
}
