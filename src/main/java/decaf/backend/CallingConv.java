package decaf.backend;

import java.util.Set;

import decaf.tac.Temp;

import decaf.backend.OffsetCounter;

// Calling convention.
// Handles calling convention like argument passing.
//
// On a call, 
//	1. a series of `addParam`s are issued, adding arguments	from left to right.
//	2. `finishParam` is called.
//	3. `spillToStack` is called, saving caller-save registers onto the stack
//	4. The call instruction is finally emitted.
//
// Now we only have caller-save variables from t0 ~ s7.
public interface CallingConv {
	public int addParam(Temp temp);

	public void finishParam();

	public void spillToStack(Temp t);
}
