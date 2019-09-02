package decaf.backend;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import decaf.Driver;
import decaf.dataflow.BasicBlock;
import decaf.machdesc.Register;
import decaf.tac.Tac;
import decaf.tac.Temp;


// This class is basically the same as brute allocator.
public class GraphColorRegisterAllocator implements RegisterAllocator {
	private BasicBlock bb;

	private CallingConv callingConv;

	private Register[] regs;

	private Temp fp;

	public GraphColorRegisterAllocator(Temp fp, CallingConv callingConv,
			Register[] regs) {
		this.fp = fp;
		this.callingConv = callingConv;
		this.regs = regs;
	}

	public void alloc(BasicBlock bb) {
		this.bb = bb;
		clear();

		// Use InferenceGraph to do basicblock-wise register allocation here.
		// But before that, you have to do something.

		Tac tail = null;
		for (Tac tac = bb.tacList; tac != null; tail = tac, tac = tac.next) {
			switch (tac.opc) {
				case ADD:
				case SUB:
				case MUL:
				case DIV:
				case MOD:
				case LAND:
				case LOR:
				case GTR:
				case GEQ:
				case EQU:
				case NEQ:
				case LEQ:
				case LES:
					findRegForRead(tac, tac.op1);
					findRegForRead(tac, tac.op2);
					findRegForWrite(tac, tac.op0);
					break;
				case NEG:
				case LNOT:
				case ASSIGN:
					findRegForRead(tac, tac.op1);
					findRegForWrite(tac, tac.op0);
					break;
				case LOAD_VTBL:
				case LOAD_IMM4:
				case LOAD_STR_CONST:
					findRegForWrite(tac, tac.op0);
					break;
				case INDIRECT_CALL:
					findRegForRead(tac, tac.op1);
					// fallthrough
				case DIRECT_CALL:
					if (tac.op0 != null) {
						findRegForWrite(tac, tac.op0);
					}
					callingConv.finishParam();
					saveLiveOutForTac(tac);
					break;
				case PARM:
					findRegForRead(tac, tac.op0);
					int offset = callingConv.addParam(tac.op0);
					tac.op1 = Temp.createConstTemp(offset);
					break;
				case LOAD:
					findRegForRead(tac, tac.op1);
					findRegForWrite(tac, tac.op0);
					break;
				case STORE:
					findRegForRead(tac, tac.op1);
					findRegForRead(tac, tac.op0);
					break;
				case BRANCH:
				case BEQZ:
				case BNEZ:
				case RETURN:
					throw new IllegalArgumentException();
			}
		}

		saveLiveOutForBB(bb);

		switch (bb.endKind) {
			case BY_RETURN:
			case BY_BEQZ:
			case BY_BNEZ:
				if (bb.var != null) {
					if (bb.var.reg != null && bb.var.equals(bb.var.reg.var)) {
						bb.varReg = bb.var.reg;
						return;
					} else {
						// all live temps have been spilled out, so use any reg is valid
						bb.var.reg = regs[0];
						if (!bb.var.isOffsetFixed())
							throw new IllegalArgumentException(bb.var +
									" may used before define during register allocation");
						Tac load = Tac.genLoad(bb.var, fp,
								Temp.createConstTemp(bb.var.offset));
						bb.insertAfter(load, tail);
						bb.varReg = regs[0];
					}
				}
		}
	}

	private void clear() {
		for (Register reg : regs) {
			if (reg.var != null) {
				reg.var = null;
			}
		}
	}

	private void load(Tac tac, Temp temp) {
		if (!temp.isOffsetFixed())
			throw new IllegalArgumentException(bb.var +
					" may used before define during register allocation");
		Tac load = Tac.genLoad(temp, fp, Temp.createConstTemp(temp.offset));
		bb.insertBefore(load, tac);
	}

	private void bind(Register reg, Temp temp) {
		reg.var = temp;
		temp.reg = reg;
	}

	private void findReg(Tac tac, Temp temp, boolean read) {
		// We've done register allocation before, so here we bind register and values.
		if (temp.reg != null) {
			temp.reg.var = temp;
			return;
		}
		throw new IllegalArgumentException("Register allocation incomplete!");
	}

	private void findRegForRead(Tac tac, Temp temp) { findReg(tac, temp, true); }
	private void findRegForWrite(Tac tac, Temp temp) { findReg(tac, temp, false); }

	private void spill(Tac tac, Temp temp) {
		Tac spill = Tac.genStore(temp, fp, Temp.createConstTemp(temp.offset));
		bb.insertBefore(spill, tac);
	}

	private void saveLiveOutForTac(Tac tac) {
		tac.saves = new HashSet<Temp>();
		for (Temp t : tac.liveOut)
			if (t.reg != null && t.equals(t.reg.var) && !t.equals(tac.op0)) {
				callingConv.spillToStack(t);
				tac.saves.add(t);
			}
	}

	private void saveLiveOutForBB(BasicBlock bb) {
		bb.saves = new HashSet<Temp>();
		for (Temp t : bb.liveOut) {
			if (t.reg != null && t.equals(t.reg.var)) {
				callingConv.spillToStack(t);
				bb.saves.add(t);
			}
		}
	}
}
