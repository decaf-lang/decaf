package decaf.error;

import decaf.tree.Pos;

/**
 * example：'orz' is not a method in class 'Person'<br>
 * PA2
 */
public class NotClassMethodError extends DecafError {

	private String name;

	private String owner;

	public NotClassMethodError(Pos pos, String name, String owner) {
		super(pos);
		this.name = name;
		this.owner = owner;
	}

	@Override
	protected String getErrMsg() {
		return "'" + name + "' is not a method in class '" + owner + "'";
	}

}
