package decaf.tree;

/**
 * 语法符号在源代码中的位置<br>
 */
public class Pos implements Comparable<Pos> {

	public static final Pos NoPos = new Pos(-1, -1);
	/**
	 * 该符号第一个字符所在的行号
	 */
	private int line;

	/**
	 * 该符号第一个字符所在的列号
	 */
	private int column;

	/**
	 * 构造一个位置记录
	 * 
	 * @param lin
	 *            行号
	 * @param col
	 *            列号
	 */
	public Pos(int lin, int col) {
		line = lin;
		column = col;
	}

	/**
	 * 转换成(x,y)形式的字符串
	 */
	@Override
	public String toString() {
		return "(" + line + "," + column + ")";
	}

	@Override
	public int compareTo(Pos o) {
		if (line > o.line) {
			return 1;
		}
		if (line < o.line) {
			return -1;
		}
		if (column > o.column) {
			return 1;
		}
		if (column < o.column) {
			return -1;
		}
		return 0;
	}
}
