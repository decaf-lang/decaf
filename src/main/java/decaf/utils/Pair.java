package decaf.utils;

public class Pair<T1, T2> {
    public final T1 fst;
    public final T1 snd;

    public Pair(T1 fst, T1 snd) {
        this.fst = fst;
        this.snd = snd;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", fst, snd);
    }
}
