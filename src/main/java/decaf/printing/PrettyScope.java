package decaf.printing;

import decaf.frontend.scope.*;
import decaf.lowlevel.log.IndentPrinter;

/**
 * Pretty print a scope. PA2 output.
 */
public final class PrettyScope extends PrettyPrinter<Scope> {

    public PrettyScope(IndentPrinter printer) {
        super(printer);
    }

    @Override
    public void pretty(Scope scope) {
        if (scope.isGlobalScope()) {
            var globalScope = (GlobalScope) scope;
            printer.println("GLOBAL SCOPE:");
            printer.incIndent();
            if (scope.isEmpty()) printer.println("<empty>");
            else scope.forEach(printer::println);
            globalScope.nestedClassScopes().forEach(this::pretty);
            printer.decIndent();
        } else if (scope.isClassScope()) {
            var classScope = (ClassScope) scope;
            printer.formatLn("CLASS SCOPE OF '%s':", classScope.getOwner().name);
            printer.incIndent();
            if (scope.isEmpty()) printer.println("<empty>");
            else scope.forEach(printer::println);
            classScope.nestedFormalScopes().forEach(this::pretty);
            printer.decIndent();
        } else if (scope.isFormalScope()) {
            var formalScope = (FormalScope) scope;
            printer.formatLn("FORMAL SCOPE OF '%s':", formalScope.getOwner().name);
            printer.incIndent();
            if (scope.isEmpty()) printer.println("<empty>");
            else scope.forEach(printer::println);
            pretty(formalScope.nestedLocalScope());
            printer.decIndent();
        } else if (scope.isLocalScope()) {
            var localScope = (LocalScope) scope;
            printer.println("LOCAL SCOPE:");
            printer.incIndent();
            if (scope.isEmpty()) printer.println("<empty>");
            else scope.forEach(printer::println);
            localScope.nestedLocalScopes().forEach(this::pretty);
            printer.decIndent();
        }
    }
}
