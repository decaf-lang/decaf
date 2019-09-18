package decaf.lowlevel.tac;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Class info, for building virtual tables.
 */
public class ClassInfo {
    /**
     * Class name.
     */
    public final String name;

    /**
     * Name of parent class, if any.
     */
    public final Optional<String> parent;

    /**
     * Member variable names.
     */
    public final Set<String> memberVariables;

    /**
     * Member method names.
     */
    public final Set<String> memberMethods;

    /**
     * Static method names.
     */
    public final Set<String> staticMethods;

    /**
     * All method names.
     */
    public final Set<String> methods;

    /**
     * Is it main class?
     */
    public final boolean isMainClass;

    /**
     * Create a class info.
     *
     * @param name            class name
     * @param parent          name of parent class, if any
     * @param memberVariables member variable names
     * @param memberMethods   member methods names
     * @param staticMethods   static methods names
     * @param isMainClass     is it main class?
     */
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
