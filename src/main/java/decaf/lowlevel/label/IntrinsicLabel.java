package decaf.lowlevel.label;

import decaf.lowlevel.tac.Intrinsic;

/**
 * Labels for intrinsic.
 *
 * @see decaf.lowlevel.tac.Intrinsic
 */
public class IntrinsicLabel extends Label {
    /**
     * Opcode of the intrinsic.
     */
    public final Intrinsic.Opcode opcode;

    public IntrinsicLabel(String name, Intrinsic.Opcode opcode) {
        super(Kind.INTRINSIC, name);
        this.opcode = opcode;
    }

    @Override
    public boolean isIntrinsic() {
        return true;
    }
}
