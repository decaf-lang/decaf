package decaf.parsing;

import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.printing.IndentPrinter;
import decaf.printing.PrettyTree;
import decaf.tree.Pos;
import decaf.tree.Tree;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Parser extends Phase<InputStream, Tree.TopLevel> {

    public Parser(Config config) {
        super("parser", config);
    }

    @Override
    public Tree.TopLevel transform(InputStream input) {
        AbstractLexer lexer = new decaf.parsing.DecafLexer(new InputStreamReader(input));
        AbstractParser parser = new decaf.parsing.DecafParser();
        lexer.parser = parser;
        parser.lexer = lexer;
        lexer.issuer = this;
        parser.issuer = this;
        return parser.tree().orElse(new Tree.TopLevel(new ArrayList<>(), Pos.NoPos));
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
