package decaf.lowlevel.instr;

/**
 * A (physical) register.
 */
public final class Reg extends Temp {
    /**
     * Platform-specific name.
     */
    public final String name;

    /**
     * Platform-specific id.
     * <p>
     * Note that {@code id} and {@code index} are different things. Because we must distinguish registers from normal
     * temps, register indexes are all negative.
     */
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
