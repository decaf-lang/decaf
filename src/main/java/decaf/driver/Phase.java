package decaf.driver;

import java.io.PrintStream;
import java.util.Optional;

public abstract class Phase<In, Out> implements Task<In, Out>, ErrorIssuer {

    public final String name;
    protected final Config config;

    public Phase(String name, Config config) {
        this.name = name;
        this.config = config;
    }

    public abstract Out transform(In input);

    public void onSucceed(Out output) {
    }

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
