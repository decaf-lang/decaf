package decaf.instr.tac;

import decaf.instr.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProgramWriter {
    public ProgramWriter(List<ClassInfo> classes) {
        for (var clazz : classes) {
            _classes.put(clazz.name, clazz); // TODO copy clazz
        }
    }

    public void visitVTables() {
        // Allocate labels for every method, including the constructor <init>, which initializes an object.
        for (var clazz : _classes.values()) {
            _ctx.putConstructorLabel(clazz.name);
            for (var method : clazz.methods) {
                _ctx.putMethodLabel(clazz.name, method);
            }
        }

        // Build virtual tables.
        for (var clazz : _classes.values()) {
            buildVTableFor(clazz);
        }

        // Create the _NEW method for every class.
        for (var clazz : _classes.values()) {
            createConstructorFor(clazz.name);
        }
    }

    public MethodVisitor visitMainMethod() {
        var entry = TAC.MAIN_LABEL;
        return new MethodVisitor(entry, 0, _ctx);
    }

    public MethodVisitor visitMethod(String className, String methodName, int numArgs) {
        var entry = _ctx.getMethodLabel(className, methodName);
        return new MethodVisitor(entry, numArgs, _ctx);
    }

    public TAC.Prog visitEnd() {
        return new TAC.Prog(_ctx.getVTables(), _ctx.funcs);
    }

    private HashMap<String, ClassInfo> _classes = new HashMap<>();

    private Context _ctx = new Context();

    /**
     * Emit code for initializing a new object. In memory, an object takes 4 * (1 + number of member variables) bytes,
     * where:
     * - the first 4 bytes: address of its virtual table
     * - next bytes: values/references of every member variables
     *
     * @param clazz
     */
    private void createConstructorFor(String clazz) {
        var entry = _ctx.getConstructorLabel(clazz);
        var mv = new MethodVisitor(entry, 0, _ctx);

        var vtbl = _ctx.getVTable(clazz);
        var size = mv.visitLoad(vtbl.getObjectSize());
        var object = mv.visitIntrinsicCall(Intrinsic.ALLOCATE, size);
        var addr = mv.visitLoadVTable(clazz);
        mv.visitStoreTo(object, addr); // the first 4 bytes: address of its virtual table
        mv.visitReturn(object);
        mv.visitEnd();
    }

    private void buildVTableFor(ClassInfo clazz) {
        if (_ctx.hasVTable(clazz.name)) return;

        var parent = clazz.parent.map(c -> {
            buildVTableFor(_classes.get(c));
            return _ctx.getVTable(c);
        });
        var vtbl = new TAC.VTable(clazz.name, parent);

        // Member methods consist of ones that are:
        // 1. inherited from super class
        // 2. overriden by this class

        if (parent.isPresent()) {
            for (var lbl : parent.get().memberMethods) {
                var method = _ctx.getMethodName(lbl);
                if (clazz.memberMethods.contains(method)) {
                    vtbl.memberMethods.add(_ctx.getMethodLabel(clazz.name, method));
                    clazz.memberMethods.remove(method);
                } else {
                    vtbl.memberMethods.add(lbl);
                }
            }
        }

        // 3. newly declared in this class
        for (var method : clazz.memberMethods) {
            vtbl.memberMethods.add(_ctx.getMethodLabel(clazz.name, method));
        }

        // Similarly, member variables consist of ones that are:
        // 1. inherited from super class
        // 2. overriden by this class (Decaf doesn't support this, but handle it for future)

        if (parent.isPresent()) {
            for (var variable : parent.get().memberVariables) {
                clazz.memberVariables.remove(variable);
                vtbl.memberVariables.add(variable);
            }
        }

        // 3. newly declared in this class
        vtbl.memberVariables.addAll(clazz.memberVariables);

        _ctx.putVTable(vtbl);
        _ctx.putOffsets(vtbl);
    }

    class Context {

        void putConstructorLabel(String clazz) {
            putLabel(clazz + ".<init>");
        }

        Label getConstructorLabel(String clazz) {
            return getLabel(clazz + ".<init>");
        }

        void putMethodLabel(String clazz, String method) {
            putLabel(clazz + "." + method);
        }

        Label getMethodLabel(String clazz, String method) {
            return getLabel(clazz + "." + method);
        }

        String getMethodName(Label method) {
            var index = method.name.indexOf(".");
            assert index >= 0;
            return method.name.substring(index + 1);
        }

        void putLabel(String name) {
            _labels.put(name, new Label(name));
        }

        Label getLabel(String name) {
            return _labels.get(name);
        }

        Label freshLabel() {
            var name = ".L" + _next_unnamed_label_id;
            _next_unnamed_label_id++;
            var lbl = new Label(name);
            _labels.put(name, lbl);
            return lbl;
        }

        TAC.VTable getVTable(String clazz) {
            return _vtables.get(clazz);
        }

        boolean hasVTable(String clazz) {
            return _vtables.containsKey(clazz);
        }

        void putVTable(TAC.VTable vtbl) {
            _vtables.put(vtbl.className, vtbl);
        }

        List<TAC.VTable> getVTables() {
            return new ArrayList<>(_vtables.values());
        }

        int getOffset(String clazz, String member) {
            return _offsets.get(clazz + "." + member);
        }

        void putOffsets(TAC.VTable vtbl) {
            var offset = 8;
            for (var method : vtbl.memberMethods) {
                _offsets.put(method.name, offset);
                offset += 4;
            }

            var prefix = vtbl.className + ".";
            offset = 4;
            for (var variable : vtbl.memberVariables) {
                _offsets.put(prefix + variable, offset);
                offset += 4;
            }
        }

        private HashMap<String, Label> _labels = new HashMap<>();

        private HashMap<String, TAC.VTable> _vtables = new HashMap<>();

        private HashMap<String, Integer> _offsets = new HashMap<>();

        List<TAC.Func> funcs = new ArrayList<>();

        private int _next_unnamed_label_id = 1;
    }

}
