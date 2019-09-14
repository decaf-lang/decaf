package decaf.dataflow;

import decaf.instr.InstrLike;
import decaf.instr.Label;
import decaf.instr.Temp;

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
        for (var loc : locs) {
            this.locs.add(loc);
        }
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
            public Loc next() {
                var loc = locs.get(i);
                i--;
                return loc;
            }
        };
    }

    public I getLastInstr() {
        if (kind.equals(Kind.BY_RETURN)) {
            throw new IllegalArgumentException("cannot get the last instruction of a return block");
        }

        if (locs.isEmpty()) {
            throw new IllegalArgumentException("locs is empty");
        }

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
        pw.println("BASIC BLOCK " + bbNum + " : ");
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
