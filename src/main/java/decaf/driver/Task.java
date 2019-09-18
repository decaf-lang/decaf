package decaf.driver;

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a "task" function that accepts one argument and may produce a result. Can be regarded as a "partial"
 * function.
 */
public interface Task<T, R> extends Function<T, Optional<R>> {
    /**
     * Pipe two tasks. This will return a function which does "this" first, if succeeds, continue do {@code next} with
     * the previous result as input; or else exits and returns {@link Optional#empty}.
     * <p>
     * In terms of monad, this is just a Kleisli composition.
     *
     * @param next the next function
     * @return the piped (Kleisli-composed) function
     */
    default <V> Task<T, V> then(Task<R, V> next) {
        return t -> this.apply(t).flatMap(next);
    }
}
