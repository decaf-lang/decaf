// Decaf lexer grammar

package decaf.frontend.parsing;

import decaf.frontend.tree.Pos;
import decaf.driver.error.*;
import decaf.lowlevel.StringUtils;

%%
%public
%class DecafLexer<P extends AbstractParser>
%extends AbstractLexer<P>
%byaccj
%line
%column
%unicode

%{
    private Pos startPos = null;
    private StringBuilder buffer = new StringBuilder();

    public Pos getPos() {
        return new Pos(yyline + 1, yycolumn + 1);
    }
%}

NEWLINE             = (\r|\n|\r\n)
DIGIT               = ([0-9])
HEX_DIGIT           = ([0-9A-Fa-f])
HEX_INTEGER         = (0[Xx]{HEX_DIGIT}+)
DEC_INTEGER         = ({DIGIT}+)
INTEGER             = ({HEX_INTEGER}|{DEC_INTEGER})
IDENTIFIER          = ([A-Za-z][_0-9A-Za-z]*)
SIMPLE_OPERATOR     = ("+"|"-"|"*"|"/"|"%"|"="|"<"|">"|"."|","|";"|"!"|"("|")"|"["|"]"|"{"|"}")
S_COMMENT           = ("//"[^\r\n]*{NEWLINE})
WHITESPACE          = ([ \t]+)
ESC                 = "\\"[nrt\"\\]
BAD_ESC             = "\\"[^nrt\"\\]

    // start token: string
%x S

%%
    // whitespace and new lines
{WHITESPACE}        { /* Just ignore */ }
{NEWLINE}           { /* Just ignore */ }
{S_COMMENT}         { /* Just ignore */ }

    // keywords
"void"              { return keyword(Tokens.VOID);         }
"int"               { return keyword(Tokens.INT);          }
"bool"              { return keyword(Tokens.BOOL);         }
"string"            { return keyword(Tokens.STRING);       }
"new"               { return keyword(Tokens.NEW);          }
"null"              { return keyword(Tokens.NULL);         }
"class"             { return keyword(Tokens.CLASS);        }
"extends"           { return keyword(Tokens.EXTENDS);      }
"this"              { return keyword(Tokens.THIS);         }
"while"             { return keyword(Tokens.WHILE);        }
"for"               { return keyword(Tokens.FOR);          }
"if"                { return keyword(Tokens.IF);           }
"else"              { return keyword(Tokens.ELSE);         }
"return"            { return keyword(Tokens.RETURN);       }
"break"             { return keyword(Tokens.BREAK);        }
"Print"             { return keyword(Tokens.PRINT);        }
"ReadInteger"       { return keyword(Tokens.READ_INTEGER); }
"ReadLine"          { return keyword(Tokens.READ_LINE);    }
"static"            { return keyword(Tokens.STATIC);       }
"instanceof"        { return keyword(Tokens.INSTANCE_OF);  }

    // operators, with more than one character
"<="                { return operator(Tokens.LESS_EQUAL);    }
">="                { return operator(Tokens.GREATER_EQUAL); }
"=="                { return operator(Tokens.EQUAL);         }
"!="                { return operator(Tokens.NOT_EQUAL);     }
"&&"                { return operator(Tokens.AND);           }
"||"                { return operator(Tokens.OR);            }
{SIMPLE_OPERATOR}   { return operator((int) yycharat(0));    }

    // literals
"true"              { return boolConst(true);    }
"false"             { return boolConst(false);   }
{INTEGER}           { return intConst(yytext()); }
<YYINITIAL>\"       { startPos = getPos();
                      yybegin(S);
                      buffer = new StringBuilder();
                      buffer.append('"'); }
<S>{NEWLINE}        { buffer.append(yytext());
                      issueError(new NewlineInStrError(getPos(), buffer.toString())); }
<S><<EOF>>          { issueError(new UntermStrError(startPos, buffer.toString()));
                      yybegin(YYINITIAL); }
<S>\"               { buffer.append('"');
                      yybegin(YYINITIAL);
                      return stringConst(buffer.toString(), startPos); }
<S>{ESC}            { buffer.append(yytext()); }
<S>{BAD_ESC}        { buffer.append(yytext());
                      issueError(new BadEscCharError(getPos())); }
<S>.                { buffer.append(yytext()); }

    // identifiers
{IDENTIFIER}        { return identifier(yytext()); }

    // other characters: unrecognized error
.                   { issueError(new UnrecogCharError(getPos(), yycharat(0)));      }
