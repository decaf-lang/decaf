package decaf.instr.tac;

import java.util.*;

public class ClassInfo {
    public final String name;
    public final Optional<String> parent;
    public final Set<String> memberVariables;
    public final Set<String> memberMethods;
    public final Set<String> staticMethods;
    public final Set<String> methods;
    public final boolean isMainClass;

    public ClassInfo(String name, Optional<String> parent, Set<String> memberVariables,
                     Set<String> memberMethods, Set<String> staticMethods, boolean isMainClass) {
        this.name = name;
        this.parent = parent;
        this.memberVariables = memberVariables;
        this.memberMethods = memberMethods;
        this.staticMethods = staticMethods;
        this.isMainClass = isMainClass;

        var methods = new HashSet<String>();
        methods.addAll(memberMethods);
        methods.addAll(staticMethods);
        this.methods = methods;
    }
}
