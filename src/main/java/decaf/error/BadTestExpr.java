package decaf.error;

import decaf.tree.Pos;

/**
 * example：test expression must have bool type<br>
 * PA2
 */
public class BadTestExpr extends DecafError {

	public BadTestExpr(Pos pos) {
		super(pos);
	}

	@Override
	protected String getErrMsg() {
		return "test expression must have bool type";
	}

}
