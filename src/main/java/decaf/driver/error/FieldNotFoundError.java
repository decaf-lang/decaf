package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：field 'money' not found in 'Student'<br>
 * PA2
 */
public class FieldNotFoundError extends DecafError {

    private String name;

    private String owner;

    public FieldNotFoundError(Pos pos, String name, String owner) {
        super(pos);
        this.name = name;
        this.owner = owner;
    }

    @Override
    protected String getErrMsg() {
        return "field '" + name + "' not found in '" + owner + "'";
    }

}
