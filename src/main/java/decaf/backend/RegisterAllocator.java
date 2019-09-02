package decaf.backend;

import decaf.dataflow.BasicBlock;

public interface RegisterAllocator {
	// For each tac in the bb, determine its register assignment.
	// Possibly it needs inserting spill / load etc instructions.
	public void alloc(BasicBlock bb);
}
