package decaf.parsing;

import decaf.driver.Config;
import decaf.driver.Phase;
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
        return parser.tree().orElse(new Tree.TopLevel(new ArrayList<>(), Pos.NoPos));
    }
}
