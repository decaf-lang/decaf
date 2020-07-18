package decaf.lowlevel.instr;

import decaf.lowlevel.label.Label;

/**
 * A trick to encode some holes as normal pseudo instructions.
 *
 * Apart from #CallerSave and #CallerRestore, other hole instructions are 'expanded' after register allocation.
 * This is because we currently cannot express constraints in RegAlloc (e.g. x86.idiv use edx:eax),
 * so we add instructions to move operands from/to the required registers by *expanding hole instructions*.
 */
public abstract class HoleInstr extends PseudoInstr {
    public HoleInstr() {
        super(new Temp[]{}, new Temp[]{});
    }

    public HoleInstr(Temp[] dsts, Temp[] srcs) {
        super(Kind.SEQ, dsts, srcs, null);
    }

    /**
     * A hole which indicates we need to insert instructions of caller save.
     */
    private static final String CALLER_SAVE = "# TODO: caller save";
    public final static HoleInstr CallerSave = new HoleInstr() {
        @Override
        public String toString() {
            return CALLER_SAVE;
        }
    };

    /**
     * A hole which indicates we need to insert instructions of caller restore.
     */
    private static final String CALLER_RESTORE = "# TODO: caller restore";
    public final static HoleInstr CallerRestore = new HoleInstr() {
        @Override
        public String toString() {
            return CALLER_RESTORE;
        }
    };

    public HoleInstr(Kind kind, Temp[] dsts, Temp[] srcs, Label label) {
        super(kind, dsts, srcs, label);
    }
}
