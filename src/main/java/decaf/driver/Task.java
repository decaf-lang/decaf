package decaf.driver;

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a "task" function that accepts one argument and may produce a result.
 */
public interface Task<T, R> extends Function<T, Optional<R>> {
    /**
     * Kleisli composition.
     *
     * @param next the next function
     * @return a function which does `this` function first, if succeeds, continue do `next` with the result;
     * or else exits and returns failure.
     */
    default <V> Task<T, V> then(Task<R, V> next) {
        return t -> this.apply(t).flatMap(next);
    }
}
