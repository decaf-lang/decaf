package decaf.backend.dataflow;

import decaf.lowlevel.InstrLike;
import decaf.lowlevel.Label;
import decaf.lowlevel.Temp;

import java.io.PrintWriter;
import java.util.*;

public class BasicBlock<I extends InstrLike> implements Iterable<Loc<I>> {
    public enum Kind {
        CONTINUE, BY_BRANCH, BY_COND_BRANCH, BY_RETURN
    }

    public Kind kind;

    public int bbNum;

    public final Optional<Label> label;

    public final List<Loc<I>> locs;

    public BasicBlock(Kind kind, int id, Optional<Label> label, List<Loc<I>> locs) {
        this.kind = kind;
        this.bbNum = id;
        this.label = label;
        this.locs = new ArrayList<>();
        this.locs.addAll(locs);
    }

    public boolean isEmpty() { return locs.isEmpty(); }

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
            public Loc next() {
                var loc = locs.get(i);
                i--;
                return loc;
            }
        };
    }

    public List<Loc<I>> seqLocs() {
        if (kind.equals(Kind.CONTINUE)) {
            return locs;
        }

        return locs.subList(0, locs.size() - 1);
    }

    public I getLastInstr() {
        return locs.get(locs.size() - 1).instr;
    }

    public Set<Temp> def;

    public Set<Temp> liveUse;

    public Set<Temp> liveIn;

    public Set<Temp> liveOut;

    public void printTo(PrintWriter pw) {
        pw.println("BASIC BLOCK " + bbNum + " " + label.map(Label::toString).orElse("<unnamed>") + ": ");
        for (var loc : this) {
            pw.println(loc);
        }
    }

    public void printLivenessTo(PrintWriter pw) {
        pw.println("BASIC BLOCK " + bbNum + " " + label.map(Label::toString).orElse("<unnamed>") + ": ");
        pw.println("  Def     = " + setToString(def));
        pw.println("  liveUse = " + setToString(liveUse));
        pw.println("  liveIn  = " + setToString(liveIn));
        pw.println("  liveOut = " + setToString(liveOut));

        for (var loc : this) {
            pw.println(loc);
        }
    }

    public static String setToString(Set<Temp> set) {
        StringBuilder sb = new StringBuilder("[ ");
        for (var t : set) {
            sb.append(t + " ");
        }
        sb.append(']');
        return sb.toString();
    }
}
