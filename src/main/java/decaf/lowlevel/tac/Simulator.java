package decaf.lowlevel.tac;

import decaf.lowlevel.TacInstr;
import decaf.lowlevel.Temp;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

/**
 * TAC program simulator.
 */
public final class Simulator {

    public Simulator(InputStream in, OutputStream out) {
        _in = in;
        _out = new PrintWriter(out);
    }

    public void execute(TAC.Prog program) {
        // Initialize
        _memory = new Memory();
        _string_pool = new StringPool();
        _vtable_to_addr = new HashMap<>();
        _instrs = new Vector<>();
        _label_to_addr = new HashMap<>();
        _addr_to_function = new HashMap<>();
        _call_stack = new Stack<>();
        _actual_args = new Vector<>();
        _label_to_function = new HashMap<>();
        for (var func : Intrinsic.ALL) {
            _label_to_function.put(func.entry.name, func);
        }

        // Allocate vtables
        for (TAC.VTable vtbl : program.vtables) {
            var addr = _memory.alloc(vtbl.getSize());
            _vtable_to_addr.put(vtbl.label.name, addr);
        }

        // Load instructions
        var addr = 0;
        for (var func : program.funcs) {
            // Meet a function: label -> func, label -> addr, addr -> func
            _label_to_function.put(func.entry.name, func);
            _addr_to_function.put(addr, func);

            // Add every non-pseudo instruction, and record labels if necessary
            for (var instr : func.getInstrSeq()) {
                if (instr.isLabel()) { // meet a label
                    var lbl = instr.jumpTo;
                    _label_to_addr.put(lbl.name, addr);
                } else {
                    var ins = (TacInstr) instr;
                    _instrs.add(ins);
                    addr++;
                } // else: memo, ignore
            }

            // Force this function to return if the last instruction is not RETURN
            if (!_instrs.lastElement().isReturn()) {
                _instrs.add(new TacInstr.Return());
                addr++;
            }
        }

        // Fill in vtables
        for (TAC.VTable vtbl : program.vtables) {
            addr = _vtable_to_addr.get(vtbl.label.name);
            var offset = 0;

            var parentAddr = vtbl.parent.map(pv -> _vtable_to_addr.get(pv.label.name)).orElse(0);
            _memory.store(parentAddr, addr, offset);
            offset += 4;

            var className = _string_pool.add(vtbl.className);
            _memory.store(className, addr, 4);
            offset += 4;

            for (var item : vtbl.getItems()) {
                _memory.store(_label_to_addr.get(item.name), addr, offset);
                offset += 4;
            }
        }

        // Initialize call stack and push the frame of main function
        if (!_label_to_function.containsKey(TAC.MAIN_LABEL.name)) {
            System.err.println("Main function not found.");
            return;
        }

        var frame = new Frame(_label_to_function.get(TAC.MAIN_LABEL.name));
        _call_stack.push(frame);
        _pc = _label_to_addr.get(TAC.MAIN_LABEL.name);

        // Execute
        var executor = new InstrExecutor();
        while (!_call_stack.isEmpty()) {
            _instrs.get(_pc).accept(executor);
        }
    }

    /**
     * IO.
     */
    private final InputStream _in;
    private final PrintWriter _out;

    /**
     * Memory.
     */
    private Memory _memory;

    /**
     * String pool: either constant or read from stdin.
     */
    private StringPool _string_pool;

    /**
     * Look up a vtable's address in memory by its name.
     */
    private HashMap<String, Integer> _vtable_to_addr;

    /**
     * Simulate instruction memory. The "address" is simply the index of this vector.
     */
    private Vector<TacInstr> _instrs;

    /**
     * Look up a label's address in instruction memory by its name.
     */
    private HashMap<String, Integer> _label_to_addr;

    /**
     * Look up a function by its entry label.
     */
    private HashMap<String, TAC.Func> _label_to_function;

    /**
     * Look up a function by the address of its entry instruction.
     */
    private HashMap<Integer, TAC.Func> _addr_to_function;

    /**
     * Call stack, consists of frames.
     */
    private Stack<Frame> _call_stack;

    /**
     * Temporarily save the actual arguments given by the PARM instruction. These will be erased once a new stack
     * is created.
     */
    private Vector<Integer> _actual_args;

    /**
     * Program counter: point to the address of the instruction being executed.
     */
    private int _pc;

    /**
     * Stack frame.
     */
    private class Frame {
        /**
         * The function we call.
         */
        final TAC.Func func;

        /**
         * An array to store values of local temps.
         */
        int[] array;

        /**
         * Save: which temp to write the return value.
         */
        Temp retValDst;

        /**
         * Save: the address of the next instruction to be executed once the function call returns.
         */
        int pcNext;

        Frame(TAC.Func func) {
            this.func = func;
            this.array = new int[func.getUsedTempCount()];
            var i = 0;
            for (var arg : _actual_args) { // copy actual arguments
                this.array[i] = arg;
                i++;
            }
            _actual_args.clear(); // it will save args for future calls
        }
    }

    /**
     * Instruction executor.
     */
    private class InstrExecutor implements TacInstr.InstrVisitor {
        @Override
        public void visitAssign(TacInstr.Assign instr) {
            var frame = _call_stack.peek();
            frame.array[instr.dst.index] = frame.array[instr.src.index];

            _pc++;
        }

        @Override
        public void visitLoadVTbl(TacInstr.LoadVTbl instr) {
            var frame = _call_stack.peek();
            frame.array[instr.dst.index] = _vtable_to_addr.get(instr.vtbl.label.name);

            _pc++;
        }

        @Override
        public void visitLoadImm4(TacInstr.LoadImm4 instr) {
            var frame = _call_stack.peek();
            frame.array[instr.dst.index] = instr.value;

            _pc++;
        }

        @Override
        public void visitLoadStrConst(TacInstr.LoadStrConst instr) {
            var frame = _call_stack.peek();
            var index = _string_pool.add(instr.value);
            frame.array[instr.dst.index] = index;

            _pc++;
        }

        @Override
        public void visitUnary(TacInstr.Unary instr) {
            var frame = _call_stack.peek();
            int operand = frame.array[instr.operand.index];
            frame.array[instr.dst.index] = switch (instr.kind) {
                case NEG -> -operand;
                case LNOT -> (operand == 0) ? 1 : 0;
            };

            _pc++;
        }

        @Override
        public void visitBinary(TacInstr.Binary instr) {
            var frame = _call_stack.peek();
            var lhs = frame.array[instr.lhs.index];
            var rhs = frame.array[instr.rhs.index];
            frame.array[instr.dst.index] = switch (instr.kind) {
                case ADD -> lhs + rhs;
                case SUB -> lhs - rhs;
                case MUL -> lhs * rhs;
                case DIV -> lhs / rhs;
                case MOD -> lhs % rhs;
                case EQU -> (lhs == rhs) ? 1 : 0;
                case NEQ -> (lhs != rhs) ? 1 : 0;
                case LES -> (lhs < rhs) ? 1 : 0;
                case LEQ -> (lhs <= rhs) ? 1 : 0;
                case GTR -> (lhs > rhs) ? 1 : 0;
                case GEQ -> (lhs >= rhs) ? 1 : 0;
                case LAND -> (lhs == 0) ? 0 : (rhs == 0) ? 0 : 1;
                case LOR -> (lhs != 0) ? 1 : (rhs == 0) ? 0 : 1;
            };

            _pc++;
        }

        @Override
        public void visitBranch(TacInstr.Branch instr) {
            _pc = _label_to_addr.get(instr.target.name);
        }

        @Override
        public void visitCondBranch(TacInstr.CondBranch instr) {
            var frame = _call_stack.peek();
            var jump = switch (instr.kind) {
                case BEQZ -> frame.array[instr.cond.index] == 0;
                case BNEZ -> frame.array[instr.cond.index] != 0;
            };

            if (jump) {
                _pc = _label_to_addr.get(instr.target.name);
            } else {
                _pc++;
            }
        }

        @Override
        public void visitReturn(TacInstr.Return instr) {
            var value = instr.value.map(temp -> _call_stack.peek().array[temp.index]);
            returnWith(value);
        }

        private void returnWith(Optional<Integer> value) {
            // Destroy the callee's frame
            _call_stack.pop();

            // Recover caller's state, if the caller exists
            if (!_call_stack.isEmpty()) {
                var frame = _call_stack.peek();
                value.ifPresent(v -> frame.array[frame.retValDst.index] = v);
                _pc = _call_stack.peek().pcNext;
            } // else: the entire program terminates
        }

        @Override
        public void visitParm(TacInstr.Parm instr) {
            var frame = _call_stack.peek();
            _actual_args.add(frame.array[instr.value.index]);

            _pc++;
        }

        @Override
        public void visitIndirectCall(TacInstr.IndirectCall instr) {
            // Save caller's state
            var frame = _call_stack.peek();
            frame.pcNext = _pc + 1;
            frame.retValDst = instr.dst.orElse(null);

            // Create callee's frame and invoke
            var addr = frame.array[instr.entry.index];
            var func = _addr_to_function.get(addr);
            _call_stack.push(new Frame(func));
            _pc = addr;
        }

        @Override
        public void visitDirectCall(TacInstr.DirectCall instr) {
            // Save caller's state
            var frame = _call_stack.peek();
            frame.pcNext = _pc + 1;
            frame.retValDst = instr.dst.orElse(null);

            // Create callee's frame and invoke
            var func = _label_to_function.get(instr.entry.name);
            _call_stack.push(new Frame(func));
            if (func.isIntrinsic()) {
                callIntrinsic((Intrinsic) func);
            } else {
                _pc = _label_to_addr.get(instr.entry.name);
            }
        }

        private void callIntrinsic(Intrinsic func) {
            var frame = _call_stack.peek();
            Optional<Integer> retVal = Optional.empty();

            switch (func.kind) {
                case ALLOCATE -> retVal = Optional.of(_memory.alloc(frame.array[0]));
                case READ_LINE -> {
                    var scanner = new Scanner(_in);
                    var str = scanner.nextLine();
                    assert str.length() <= 63;
                    retVal = Optional.of(_string_pool.add(str));
                }
                case READ_INT -> {
                    var scanner = new Scanner(_in);
                    var value = scanner.nextInt();
                    retVal = Optional.of(value);
                }
                case STRING_EQUAL -> retVal = Optional.of(frame.array[0] == frame.array[1] ? 1 : 0);
                case PRINT_INT -> {
                    _out.print(frame.array[0]);
                    _out.flush();
                }
                case PRINT_STRING -> {
                    _out.write(_string_pool.get(frame.array[0]));
                    _out.flush();
                }
                case PRINT_BOOL -> {
                    _out.write(frame.array[0] == 0 ? "false" : "true");
                    _out.flush();
                }
                case HALT -> System.exit(0); // TODO: just stop the execution of the simulator
            }

            returnWith(retVal);
        }

        @Override
        public void visitMemory(TacInstr.Memory instr) {
            var frame = _call_stack.peek();
            int base = frame.array[instr.base.index];
            int offset = instr.offset;
            switch (instr.kind) {
                case LOAD -> frame.array[instr.dst.index] = _memory.load(base, offset);
                case STORE -> _memory.store(frame.array[instr.dst.index], base, offset);
            }

            _pc++;
        }
    }

    /**
     * Memory.
     */
    private class Memory {
        /**
         * Don't start from address 0, because 0 is reserved as the null pointer.
         */
        private int currentSize = 4;

        private class Block implements Comparable<Block> {

            public int start;

            public int[] mem;

            @Override
            public int compareTo(Block o) {
                return start > o.start ? 1 : start == o.start ? 0 : -1;
            }
        }

        /**
         * A heap is a list of memory blocks. It stores the actual data.
         */
        private List<Block> heap = new ArrayList<>();

        /**
         * Allocate memory in bytes.
         *
         * @param size the size in bytes.
         * @return the starting address of the allocated memory block
         */
        public int alloc(int size) {
            if (size < 0 || size % 4 != 0) {
                throw new ExecError("bad alloc size = " + size);
            }
            size /= 4;
            Block block = new Block();
            block.start = currentSize;
            currentSize += size;
            block.mem = new int[size];
            heap.add(block);
            return block.start * 4;
        }

        private Block checkHeapAccess(int base, int offset) {
            if (base < 0 || base % 4 != 0 || offset % 4 != 0) {
                throw new ExecError("bad memory access base = " + base
                        + " offset = " + offset);
            }
            base /= 4;
            offset /= 4;
            if (base >= currentSize) {
                throw new ExecError("memory access base = " + base * 4
                        + " out of bounds");
            }
            Block temp = new Block();
            temp.start = base;
            int index = Collections.binarySearch(heap, temp);
            Block block = index >= 0 ? heap.get(index) : heap.get(-index - 2);
            int accessIndex = base - block.start + offset;
            if (accessIndex < 0 || accessIndex >= block.mem.length) {
                throw new ExecError("memory access base = " + base * 4
                        + " offset = " + offset * 4 + " out of bounds");
            }
            return block;
        }

        public int load(int base, int offset) {
            Block block = checkHeapAccess(base, offset);
            return block.mem[base / 4 - block.start + offset / 4];
        }

        public void store(int value, int base, int offset) {
            Block block = checkHeapAccess(base, offset);
            block.mem[base / 4 - block.start + offset / 4] = value;
        }
    }
}
