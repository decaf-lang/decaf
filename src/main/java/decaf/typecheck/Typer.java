package decaf.typecheck;

import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.tree.Tree;

public class Typer extends Phase<Tree.TopLevel, Tree.TopLevel> {

    public Typer(Config config) {
        super("typer", config);
    }

    @Override
    public Tree.TopLevel transform(Tree.TopLevel input) {
        return null;
    }
}