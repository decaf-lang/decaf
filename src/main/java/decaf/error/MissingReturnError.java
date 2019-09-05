package decaf.error;

import decaf.tree.Pos;

// Typer error

public class MissingReturnError extends DecafError {

    public MissingReturnError(Pos pos) {
        super(pos);
    }

    @Override
    protected String getErrMsg() {
        return "missing return statement: control reaches end of non-void block";
    }
}
