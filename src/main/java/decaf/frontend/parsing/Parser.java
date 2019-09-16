package decaf.frontend.parsing;

import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.frontend.tree.Tree;
import decaf.printing.IndentPrinter;
import decaf.printing.PrettyTree;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The parser phase: parse a Decaf source and build an abstract syntax tree.
 * <p>
 * Hint: make sure {@code jflex} and {@code jacc} are executed first, otherwise your IDE may be unhappy with some
 * symbols.
 */
public class Parser extends Phase<InputStream, Tree.TopLevel> {

    public Parser(Config config) {
        super("parser", config);
    }

    @Override
    public Tree.TopLevel transform(InputStream input) {
        AbstractLexer lexer = new decaf.frontend.parsing.DecafLexer(new InputStreamReader(input));
        AbstractParser parser = new decaf.frontend.parsing.DecafParser();
        lexer.setup(parser, this);
        parser.setup(lexer, this);
        parser.parse();
        return parser.tree;
    }

    @Override
    public void onSucceed(Tree.TopLevel tree) {
        if (config.target.equals(Config.Target.PA1)) {
            var printer = new PrettyTree(new IndentPrinter(config.output));
            printer.pretty(tree);
            printer.flush();
        }
    }
}
