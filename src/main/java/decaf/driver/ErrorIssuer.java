package decaf.driver;

import decaf.driver.error.DecafError;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;

public interface ErrorIssuer {
    ArrayList<DecafError> errors = new ArrayList<>();

    default void issue(DecafError error) {
        errors.add(error);
    }

    default boolean hasError() {
        return !errors.isEmpty();
    }

    default void printErrors(PrintStream to) {
        errors.sort(Comparator.comparing(o -> o.pos));
        errors.forEach(to::println);
    }
}
