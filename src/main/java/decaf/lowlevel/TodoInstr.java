package decaf.lowlevel;

public class TodoInstr extends PseudoInstr {

    public boolean isCallerSave() {
        return op.equals(CALLER_SAVE);
    }

    public boolean isCallerRestore() {
        return op.equals(CALLER_RESTORE);
    }

    private TodoInstr(String op, Object... imms) {
        super(Kind.SEQ, op, new Temp[]{}, new Temp[]{}, null, imms);
    }

    private static final String CALLER_SAVE = "# TODO: caller save";
    private static final String CALLER_RESTORE = "# TODO: caller restore";

    public static TodoInstr callerSave() {
        return new TodoInstr(CALLER_SAVE);
    }

    public static TodoInstr callerRestore() {
        return new TodoInstr(CALLER_RESTORE);
    }

    @Override
    public boolean isTodo() {
        return true;
    }

    @Override
    public String getFormat() {
        throw new IllegalCallerException();
    }

    @Override
    public Object[] getArgs() {
        throw new IllegalCallerException();
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(op);
        for (var arg : imms) {
            sb.append(' ');
            sb.append(arg);
        }
        return sb.toString();
    }
}
