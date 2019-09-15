package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：field 'homework' of 'Others' not accessible here<br>
 * PA2
 */
public class FieldNotAccessError extends DecafError {

    private String name;

    private String owner;

    public FieldNotAccessError(Pos pos, String name, String owner) {
        super(pos);
        this.name = name;
        this.owner = owner;
    }

    @Override
    protected String getErrMsg() {
        return "field '" + name + "' of '" + owner + "' not accessible here";
    }

}
