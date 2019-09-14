package decaf.instr;

public final class Reg extends Temp {

    public final String name;

    public final int id;

    public Reg(int id, String name) {
        super(-id - 1); // to distinguish from normal temps (whose index start from 0)
        this.id = id; // NOT index!
        this.name = name;
    }

    public boolean isUsed() {
        return used;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean occupied = false;

    public boolean used = false;

    public Temp temp;
}
