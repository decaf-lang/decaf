package decaf.typecheck;

import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.tree.Tree;

public class Namer extends Phase<Tree.TopLevel, Tree.TopLevel> {

    public Namer(Config config) {
        super("namer", config);
    }

    @Override
    public Tree.TopLevel transform(Tree.TopLevel input) {
        return null;
    }
}
