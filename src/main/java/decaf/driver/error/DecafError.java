package decaf.driver.error;

import decaf.frontend.tree.Pos;

/**
 * decaf中所有编译错误的基类
 */
public abstract class DecafError {

    /**
     * 编译错误所在的位置
     */
    public Pos pos;

    /**
     * @return 返回错误的具体描述
     */
    protected abstract String getErrMsg();

    public DecafError(Pos pos) {
        this.pos = pos;
    }

    public Pos getPos() {
        return pos;
    }

    /**
     * 返回包含位置信息在内的完整错误信息
     */
    @Override
    public String toString() {
        if (pos.equals(Pos.NoPos)) {
            return "*** Error: " + getErrMsg();
        } else {
            return "*** Error at " + pos + ": " + getErrMsg();
        }
    }

}
