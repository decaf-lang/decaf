package decaf.driver;

import decaf.parsing.Parser;
import decaf.tree.Tree;
import decaf.typecheck.Namer;
import decaf.typecheck.Typer;

import java.io.InputStream;

public class TaskFactory {
    private final Config config;

    public TaskFactory(Config config) {
        this.config = config;
    }

    public Task<InputStream, Tree.TopLevel> parse() {
        return new Parser(config);
    }

    public Task<InputStream, Tree.TopLevel> typeCheck() {
        return parse().then(new Namer(config)).then(new Typer(config));
    }
}
