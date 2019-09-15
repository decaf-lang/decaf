package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：cannot access field 'homework' from 'Others'<br>
 * 指通过类名来访问类成员，Others是类名<br>
 * example：cannot access field 'homework' from 'int[]'<br>
 * 指通过非类成员变量来访问类成员，int[]是该变量的类型名字<br>
 * PA2
 */
public class NotClassFieldError extends DecafError {

    private String name;

    private String owner;

    public NotClassFieldError(Pos pos, String name, String owner) {
        super(pos);
        this.name = name;
        this.owner = owner;
    }

    @Override
    protected String getErrMsg() {
        return "cannot access field '" + name + "' from '" + owner + "'";
    }

}
