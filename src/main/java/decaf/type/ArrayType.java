package decaf.type;

public class ArrayType extends Type {

	public final Type elementType;

	public ArrayType(Type elementType) {
		this.elementType = elementType;
	}

	@Override
	public boolean subtypeOf(Type type) {
		if (type.eq(BuiltInType.ERROR)) {
			return true;
		}
		return eq(type);
	}

	@Override
	public boolean eq(Type type) {
		if (!type.isArrayType()) {
			return false;
		}
		return elementType.eq(((ArrayType) type).elementType);
	}

	@Override
	public String toString() {
		return elementType + "[]";
	}

	@Override
	public boolean isArrayType() {
		return true;
	}
}
