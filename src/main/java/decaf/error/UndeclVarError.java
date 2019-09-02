package decaf.error;

import decaf.tree.Pos;

/**
 * example：undeclared variable 'python'<br>
 * PA2
 */
public class UndeclVarError extends DecafError {

	private String name;

	public UndeclVarError(Pos pos, String name) {
		super(pos);
		this.name = name;
	}

	@Override
	protected String getErrMsg() {
		return "undeclared variable '" + name + "'";
	}

}
