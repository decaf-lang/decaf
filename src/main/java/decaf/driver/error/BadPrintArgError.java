package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * example：incompatible argument 3: int[] given, int/bool/string expected<br>
 * 3表示发生错误的是第三个参数<br>
 * PA2
 */
public class BadPrintArgError extends DecafError {

    private String count;

    private String type;

    public BadPrintArgError(Pos pos, String count, String type) {
        super(pos);
        this.count = count;
        this.type = type;
    }

    @Override
    protected String getErrMsg() {
        return "incompatible argument " + count + ": " + type
                + " given, int/bool/string expected";
    }

}
