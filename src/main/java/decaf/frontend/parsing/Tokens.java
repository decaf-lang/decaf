package decaf.frontend.parsing;

/**
 * Standard encoding of all Decaf tokens.
 * NOTE: MUST use ASCII code to encode a single-character token.
 */
public interface Tokens {
    int VOID = 1;
    int BOOL = 2;
    int INT = 3;
    int STRING = 4;
    int CLASS = 5;
    int NULL = 6;
    int EXTENDS = 7;
    int THIS = 8;
    int WHILE = 9;
    int FOR = 10;
    int IF = 11;
    int ELSE = 12;
    int RETURN = 13;
    int BREAK = 14;
    int NEW = 15;
    int PRINT = 16;
    int READ_INTEGER = 17;
    int READ_LINE = 18;
    int BOOL_LIT = 19;
    int INT_LIT = 20;
    int STRING_LIT = 21;
    int IDENTIFIER = 22;
    int AND = 23;
    int OR = 24;
    int STATIC = 25;
    int INSTANCE_OF = 26;
    int LESS_EQUAL = 27;
    int GREATER_EQUAL = 28;
    int EQUAL = 29;
    int NOT_EQUAL = 30;

    // MUST use ASCII code to encode a single-character token.
    // '!' (code=33)
    // '%' (code=37)
    // '(' (code=40)
    // ')' (code=41)
    // '*' (code=42)
    // '+' (code=43)
    // ',' (code=44)
    // '-' (code=45)
    // '.' (code=46)
    // '/' (code=47)
    // ';' (code=59)
    // '<' (code=60)
    // '=' (code=61)
    // '>' (code=62)
    // '[' (code=91)
    // ']' (code=93)
    // '{' (code=123)
    // '}' (code=125)
}
