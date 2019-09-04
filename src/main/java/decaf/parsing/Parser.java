package decaf.parsing;

import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.printing.IndentPrinter;
import decaf.printing.PrettyTree;
import decaf.tree.Tree;

import java.io.InputStream;
import java.io.InputStreamReader;

public class Parser extends Phase<InputStream, Tree.TopLevel> {

    public Parser(Config config) {
        super("parser", config);
    }

    @Override
    public Tree.TopLevel transform(InputStream input) {
        AbstractLexer lexer = new decaf.parsing.DecafLexer(new InputStreamReader(input));
        AbstractParser parser = new decaf.parsing.DecafParser();
        lexer.setup(parser, this);
        parser.setup(lexer, this);
        parser.parse();
        return parser.tree;
    }

    @Override
    public void onSucceed(Tree.TopLevel tree) {
        if (config.target.equals(Config.Target.PA1)) {
            var printer = new PrettyTree(new IndentPrinter(config.outputStream));
            printer.pretty(tree);
            printer.close();
        }
    }
}
