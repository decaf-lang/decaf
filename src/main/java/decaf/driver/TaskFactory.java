package decaf.driver;

import decaf.parsing.Parser;
import decaf.tacgen.TacGen;
import decaf.tools.tac.Tac;
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

    public Task<InputStream, Tac.Prog> tacGen() {
        return typeCheck().then(new TacGen(config));
    }
}
