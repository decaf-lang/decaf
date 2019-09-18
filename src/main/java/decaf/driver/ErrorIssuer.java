package decaf.driver;

import decaf.driver.error.DecafError;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Decaf error issuer. The error must be a subclass of {@link DecafError}.
 */
public interface ErrorIssuer {
    ArrayList<DecafError> errors = new ArrayList<>();

    /**
     * Add an error.
     *
     * @param error Decaf error
     */
    default void issue(DecafError error) {
        errors.add(error);
    }

    /**
     * Has any error been added?
     *
     * @return true/false
     */
    default boolean hasError() {
        return !errors.isEmpty();
    }

    /**
     * Print out error messages, sorted by their error positions.
     *
     * @param to where to print
     */
    default void printErrors(PrintStream to) {
        errors.sort(Comparator.comparing(o -> o.pos));
        errors.forEach(to::println);
    }
}
