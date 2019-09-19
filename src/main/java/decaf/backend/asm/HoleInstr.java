package decaf.backend.asm;

import decaf.lowlevel.instr.PseudoInstr;
import decaf.lowlevel.instr.Temp;

/**
 * A trick to encode some holes as normal pseudo instructions.
 *
 * @see #CallerSave
 * @see #CallerRestore
 */
public class HoleInstr extends PseudoInstr {
    private HoleInstr(String str) {
        super(new Temp[]{}, new Temp[]{});
        this.str = str;
    }

    private String str;

    @Override
    public String toString() {
        return str;
    }

    private static final String CALLER_SAVE = "# TODO: caller save";
    private static final String CALLER_RESTORE = "# TODO: caller restore";

    /**
     * A hole which indicates we need to insert instructions of caller save.
     */
    public final static HoleInstr CallerSave = new HoleInstr(CALLER_SAVE);

    /**
     * A hole which indicates we need to insert instructions of caller restore.
     */
    public final static HoleInstr CallerRestore = new HoleInstr(CALLER_RESTORE);
}
