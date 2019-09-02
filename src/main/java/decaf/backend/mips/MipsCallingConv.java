package decaf.backend.mips;

import java.util.Set;

import decaf.tac.Temp;

import decaf.backend.OffsetCounter;
import decaf.backend.CallingConv;

// Implements argument passing on stack.
// Return values are in $v0.
public class MipsCallingConv implements CallingConv {
	private int maxSize;

	private int currentSize;

	private int maxActualSize;

	private int currentActualSize;

	public int getStackFrameSize() {
		return maxSize + maxActualSize;
	}

	public void resetFrame() {
		maxSize = currentSize = 0;
		maxActualSize = currentActualSize = 4;
		OffsetCounter.LOCAL_OFFSET_COUNTER.reset();
	}

	public void findSlot(Set<Temp> saves) {
		for (Temp temp : saves) {
			findSlot(temp);
		}
	}

	public void findSlot(Temp temp) {
		if (temp.isOffsetFixed()) {
			return;
		}
		temp.offset = OffsetCounter.LOCAL_OFFSET_COUNTER.next(temp.size);
		currentSize += temp.size;
		if (currentSize > maxSize) {
			maxSize = currentSize;
		}
	}

	public int addParam(Temp temp) {
		int offset = currentActualSize;
		currentActualSize += temp.size;
		return offset;
	}

	public void finishParam() {
		if (currentActualSize > maxActualSize) {
			maxActualSize = currentActualSize;
		}
		currentActualSize = 4;
	}

	public void spillToStack(Temp t) {
		if (t.reg == null)
			throw new IllegalArgumentException(
				"spillToStack called on non-register operand!");
		findSlot(t);
	}
}
