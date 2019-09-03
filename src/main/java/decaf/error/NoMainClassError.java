package decaf.error;

import decaf.tree.Pos;

/**
 * example：no legal Main class named 'Main' was found<br>
 * PA2
 */
public class NoMainClassError extends DecafError {

    public NoMainClassError() {
        super(Pos.NoPos);
    }

    @Override
    protected String getErrMsg() {
        return "no legal Main class named 'Main' was found";
    }

}
