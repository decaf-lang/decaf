package decaf.tools.tac;

import java.util.*;

/**
 * TAC program simulator.
 */
public final class Simulator {

    public void execute(TacProgram program) {
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
        for (VTable vtbl : program.vtables) {
            var addr = _memory.alloc(vtbl.getSize());
            _vtable_to_addr.put(vtbl.name, addr);
        }

        // Load instructions
        var addr = 0;
        for (var func : program.functions) {
            // Meet a function: label -> func, label -> addr, addr -> func
            _label_to_function.put(func.entry.name, func);
            _addr_to_function.put(addr, func);

//            System.out.println("FUNCTION " + func.entry);

            // Add every non-pseudo instruction, and record labels if necessary
            for (var instr : func.getInstrSeq()) {
                if (instr.isMark()) { // meet a label
                    var mark = (Instr.Mark) instr;
                    _label_to_addr.put(mark.lbl.name, addr);
//                    System.out.println(addr + "\t" + instr + ":");
                } else if (!instr.isPseudo()) {
                    _instrs.add(instr);
                    addr++;
//                    System.out.println(addr + "\t" + "    " + instr);
                } // else: memo, ignore
            }

            // Force this function to return if the last instruction is not RETURN
            if (!_instrs.lastElement().isReturn()) {
                _instrs.add(new Instr.Return());
                addr++;
//                System.out.println(addr + "\t" + "    return");
            }
        }

        // Fill in vtables
        for (VTable vtbl : program.vtables) {
            addr = _vtable_to_addr.get(vtbl.name);
            var offset = 0;

            var parent = vtbl.parent.map(x -> _vtable_to_addr.get(x)).orElse(0);
            _memory.store(parent, addr, offset);
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
        if (!_label_to_function.containsKey("main")) {
            System.err.println("No function labeled 'main' is found.");
            return;
        }

        var frame = new Frame(_label_to_function.get("main"));
        _call_stack.push(frame);
        _pc = _label_to_addr.get("main");

        // Execute
        var executor = new InstrExecutor();
        while (!_call_stack.isEmpty()) {
//            System.out.println("pc=" + _pc);
            _instrs.get(_pc).accept(executor);
        }
    }

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
    private Vector<Instr> _instrs;

    /**
     * Look up a label's address in instruction memory by its name.
     */
    private HashMap<String, Integer> _label_to_addr;

    /**
     * Look up a function by its entry label.
     */
    private HashMap<String, Function> _label_to_function;

    /**
     * Look up a function by the address of its entry instruction.
     */
    private HashMap<Integer, Function> _addr_to_function;

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
        final Function func;

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

        Frame(Function func) {
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
    private class InstrExecutor implements Instr.Visitor {
        @Override
        public void visitAssign(Instr.Assign instr) {
            var frame = _call_stack.peek();
            frame.array[instr.dst.index] = frame.array[instr.src.index];

            _pc++;
        }

        @Override
        public void visitLoadVTbl(Instr.LoadVTbl instr) {
            var frame = _call_stack.peek();
            frame.array[instr.dst.index] = _vtable_to_addr.get(instr.vtbl.name);

            _pc++;
        }

        @Override
        public void visitLoadImm4(Instr.LoadImm4 instr) {
            var frame = _call_stack.peek();
            frame.array[instr.dst.index] = instr.value;

            _pc++;
        }

        @Override
        public void visitLoadStrConst(Instr.LoadStrConst instr) {
            var frame = _call_stack.peek();
            var index = _string_pool.add(instr.value);
            frame.array[instr.dst.index] = index;

            _pc++;
        }

        @Override
        public void visitUnary(Instr.Unary instr) {
            var frame = _call_stack.peek();
            int operand = frame.array[instr.operand.index];
            frame.array[instr.dst.index] = switch (instr.kind) {
                case NEG -> -operand;
                case LNOT -> (operand == 0) ? 1 : 0;
            };

            _pc++;
        }

        @Override
        public void visitBinary(Instr.Binary instr) {
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
        public void visitBranch(Instr.Branch instr) {
            _pc = _label_to_addr.get(instr.target.name);
        }

        @Override
        public void visitConditionalBranch(Instr.ConditionalBranch instr) {
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
        public void visitReturn(Instr.Return instr) {
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
        public void visitParm(Instr.Parm instr) {
            var frame = _call_stack.peek();
            _actual_args.add(frame.array[instr.value.index]);

            _pc++;
        }

        @Override
        public void visitIndirectCall(Instr.IndirectCall instr) {
            // Save caller's state
            var frame = _call_stack.peek();
            frame.pcNext = _pc + 1;
            frame.retValDst = instr.dst;

            // Create callee's frame and invoke
            var addr = frame.array[instr.entry.index];
            var func = _addr_to_function.get(addr);
            _call_stack.push(new Frame(func));
            _pc = addr;
        }

        @Override
        public void visitDirectCall(Instr.DirectCall instr) {
            // Save caller's state
            var frame = _call_stack.peek();
            frame.pcNext = _pc + 1;
            frame.retValDst = instr.dst;

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
                    var scanner = new Scanner(System.in);
                    var str = scanner.nextLine();
                    assert str.length() <= 63;
                    retVal = Optional.of(_string_pool.add(str));
                }
                case READ_INT -> {
                    var scanner = new Scanner(System.in);
                    var value = scanner.nextInt();
                    retVal = Optional.of(value);
                }
                case STRING_EQUAL -> retVal = Optional.of(frame.array[0] == frame.array[1] ? 1 : 0);
                case PRINT_INT -> System.out.print(frame.array[0]);
                case PRINT_STRING -> System.out.print(_string_pool.get(frame.array[0]));
                case PRINT_BOOL -> System.out.print(frame.array[0] == 0 ? "false" : "true");
                case HALT -> System.exit(0);
            }

            returnWith(retVal);
        }

        @Override
        public void visitMemory(Instr.Memory instr) {
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
        private int currentSize = 0;

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

    /**
     * A string pool to store string literals. The equal strings will be allocated a same index.
     */
    private class StringPool {
        /**
         * Add a string.
         *
         * @param value the string
         * @return the allocated index
         */
        public int add(String value) {
            int index = _strings.indexOf(value);
            if (index == -1) {
                _strings.add(value);
                return _strings.size() - 1;
            }
            return index;
        }

        /**
         * Get a string by index.
         *
         * @param index the index
         * @return the string
         */
        public String get(int index) {
            return _strings.get(index);
        }

        private Vector<String> _strings = new Vector<>();
    }
}
