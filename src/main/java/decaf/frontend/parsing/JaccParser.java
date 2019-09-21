package decaf.frontend.parsing;

import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.frontend.tree.Tree;
import decaf.lowlevel.log.IndentPrinter;
import decaf.printing.PrettyTree;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The parser phase: parse a Decaf source and build an abstract syntax tree.
 * <p>
 * Hint: make sure {@code jflex} and {@code jacc} are executed first, otherwise your IDE may be unhappy with some
 * symbols.
 */
public class JaccParser extends Phase<InputStream, Tree.TopLevel> {

    public JaccParser(Config config) {
        super("parser", config);
    }

    @Override
    public Tree.TopLevel transform(InputStream input) {
        var parser = new decaf.frontend.parsing.DecafJaccParser();
        var lexer = new decaf.frontend.parsing.DecafLexer<decaf.frontend.parsing.DecafJaccParser>(
                new InputStreamReader(input));
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

    static abstract class BaseParser extends AbstractParser {
        @Override
        public int tokenOf(int code) {
            return switch (code) {
                case Tokens.VOID -> decaf.frontend.parsing.JaccTokens.VOID;
                case Tokens.BOOL -> decaf.frontend.parsing.JaccTokens.BOOL;
                case Tokens.INT -> decaf.frontend.parsing.JaccTokens.INT;
                case Tokens.STRING -> decaf.frontend.parsing.JaccTokens.STRING;
                case Tokens.CLASS -> decaf.frontend.parsing.JaccTokens.CLASS;
                case Tokens.NULL -> decaf.frontend.parsing.JaccTokens.NULL;
                case Tokens.EXTENDS -> decaf.frontend.parsing.JaccTokens.EXTENDS;
                case Tokens.THIS -> decaf.frontend.parsing.JaccTokens.THIS;
                case Tokens.WHILE -> decaf.frontend.parsing.JaccTokens.WHILE;
                case Tokens.FOR -> decaf.frontend.parsing.JaccTokens.FOR;
                case Tokens.IF -> decaf.frontend.parsing.JaccTokens.IF;
                case Tokens.ELSE -> decaf.frontend.parsing.JaccTokens.ELSE;
                case Tokens.RETURN -> decaf.frontend.parsing.JaccTokens.RETURN;
                case Tokens.BREAK -> decaf.frontend.parsing.JaccTokens.BREAK;
                case Tokens.NEW -> decaf.frontend.parsing.JaccTokens.NEW;
                case Tokens.PRINT -> decaf.frontend.parsing.JaccTokens.PRINT;
                case Tokens.READ_INTEGER -> decaf.frontend.parsing.JaccTokens.READ_INTEGER;
                case Tokens.READ_LINE -> decaf.frontend.parsing.JaccTokens.READ_LINE;
                case Tokens.BOOL_LIT -> decaf.frontend.parsing.JaccTokens.BOOL_LIT;
                case Tokens.INT_LIT -> decaf.frontend.parsing.JaccTokens.INT_LIT;
                case Tokens.STRING_LIT -> decaf.frontend.parsing.JaccTokens.STRING_LIT;
                case Tokens.IDENTIFIER -> decaf.frontend.parsing.JaccTokens.IDENTIFIER;
                case Tokens.AND -> decaf.frontend.parsing.JaccTokens.AND;
                case Tokens.OR -> decaf.frontend.parsing.JaccTokens.OR;
                case Tokens.STATIC -> decaf.frontend.parsing.JaccTokens.STATIC;
                case Tokens.INSTANCE_OF -> decaf.frontend.parsing.JaccTokens.INSTANCE_OF;
                case Tokens.LESS_EQUAL -> decaf.frontend.parsing.JaccTokens.LESS_EQUAL;
                case Tokens.GREATER_EQUAL -> decaf.frontend.parsing.JaccTokens.GREATER_EQUAL;
                case Tokens.EQUAL -> decaf.frontend.parsing.JaccTokens.EQUAL;
                case Tokens.NOT_EQUAL -> decaf.frontend.parsing.JaccTokens.NOT_EQUAL;
                default -> code; // single-character, use their ASCII code!
            };
        }
    }
}
