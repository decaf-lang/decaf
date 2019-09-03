package decaf.driver;

import decaf.error.ErrorIssuer;

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
        printErrors(System.err);
        if (hasError()) return Optional.empty();
        onSucceed(out);
        return Optional.of(out);
    }
}
