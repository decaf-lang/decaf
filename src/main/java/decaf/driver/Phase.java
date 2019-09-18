package decaf.driver;

import java.io.PrintStream;
import java.util.Optional;

/**
 * Compilation of a Decaf program is processed phase-by-phase. Each phase transforms a kind of language representation
 * into another.
 *
 * @param <In>  input
 * @param <Out> output
 * @see Task
 * @see ErrorIssuer
 */
public abstract class Phase<In, Out> implements Task<In, Out>, ErrorIssuer {
    /**
     * Name.
     */
    public final String name;

    /**
     * Compiler configuration.
     */
    protected final Config config;

    public Phase(String name, Config config) {
        this.name = name;
        this.config = config;
    }

    /**
     * Entry of the actual transformation.
     *
     * @param input input
     * @return output
     */
    public abstract Out transform(In input);

    /**
     * A phase is said to be <em>successful</em>, if and only if no errors occur (i.e. {@code !hasError()}).
     * When a phase is successful, this method will be executed.
     *
     * @param output output of the transformation
     */
    public void onSucceed(Out output) {
    }

    /**
     * Entry of running the phase.
     *
     * @param in input
     * @return output (if succeeds)
     */
    @Override
    public Optional<Out> apply(In in) {
        var out = transform(in);
        if (hasError()) {
            printErrors(System.err);
            if (!config.output.equals(Config.STDOUT) && config.target.compareTo(Config.Target.PA3) <= 0) {
                printErrors(new PrintStream(config.output));
            }
            return Optional.empty();
        }

        onSucceed(out);
        return Optional.of(out);
    }
}
