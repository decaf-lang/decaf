package decaf.error;

import decaf.tree.Pos;

/**
 * 仅供Parser的yyerror函数使用
 */
public class MsgError extends DecafError {

	private String msg;

	public MsgError(Pos pos, String msg) {
		super(pos);
		this.msg = msg;
	}

	@Override
	protected String getErrMsg() {
		return msg;
	}

}
