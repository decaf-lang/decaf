package decaf.symbol;

import decaf.tree.Pos;
import decaf.tac.Temp;
import decaf.type.Type;

public class Variable extends Symbol {
	
	private int offset;
	
	private Temp temp;
	
	public Temp getTemp() {
		return temp;
	}

	public void setTemp(Temp temp) {
		this.temp = temp;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public Variable(String name, Type type, Pos pos) {
		this.name = name;
		this.type = type;
		this.pos = pos;
	}

	public boolean isLocalVar() {
		return definedIn.isLocalScope();
	}

	public boolean isMemberVar() {
		return definedIn.isClassScope();
	}

	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	public String toString() {
		return pos + " -> variable " + (isParam() ? "@" : "") + name
				+ " : " + type;
	}

	public boolean isParam() {
		return definedIn.isFormalScope();
	}

	@Override
	public boolean isClass() {
		return false;
	}

	@Override
	public boolean isFunction() {
		return false;
	}

}
