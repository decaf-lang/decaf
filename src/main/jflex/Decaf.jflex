/*
 * 本文件是构造Decaf编译器所需要的JFlex输入脚本。
 * 在第一阶段，你需要完成这个脚本的内容，请参考"JFlex Manual"中关于如何编写JFlex脚本的说明。
 *
 * 注意：在UNIX系统下你需要保证这个文件使用UNIX文本格式，可使用dos2unix命令进行文本各式转换。
 */

package decaf.parsing;

import decaf.tree.Pos;
import decaf.error.*;
import decaf.utils.MiscUtils;
 
%%
%public
%class DecafLexer
%extends AbstractLexer
%byaccj
%line
%column
%unicode

%{
	private Pos sloc = null;
	private StringBuilder buffer = new StringBuilder();

	public Pos getPos() {
		return new Pos(yyline + 1, yycolumn + 1);
	}
%}

NEWLINE				= (\r|\n|\r\n)
DIGIT 				= ([0-9])
HEX_DIGIT 			= ([0-9A-Fa-f])
HEX_INTEGER			= (0[Xx]{HEX_DIGIT}+)
DEC_INTEGER			= ({DIGIT}+)
INTEGER				= ({HEX_INTEGER}|{DEC_INTEGER})
IDENTIFIER			= ([A-Za-z][_0-9A-Za-z]*)
SIMPLE_OPERATOR		= ("+"|"-"|"*"|"/"|"%"|"="|"<"|">"|"."|","|";"|"!"|"("|")"|"["|"]"|"{"|"}")
S_COMMENT			= ("//"[^\r\n]*{NEWLINE})
WHITESPACE			= ([ \t]+)

	// 开始条件S表示字符串
%x S

%%
	// 识别注释和空白字符的规则
{WHITESPACE}		{ /* Just ignore */    }
{NEWLINE}			{ /* Just ignore */    }
{S_COMMENT}			{ /* Just ignore */    }


	// 识别关键字的规则 
"void"				{ return keyword(Tokens.VOID);			}
"int"				{ return keyword(Tokens.INT);			}
"bool"				{ return keyword(Tokens.BOOL);			}
"string"			{ return keyword(Tokens.STRING);		}
"new"				{ return keyword(Tokens.NEW);			}
"null"				{ return keyword(Tokens.NULL);			}
"class"				{ return keyword(Tokens.CLASS);			}
"extends"			{ return keyword(Tokens.EXTENDS);		}
"this"				{ return keyword(Tokens.THIS);			}
"while"				{ return keyword(Tokens.WHILE);			}
"for"				{ return keyword(Tokens.FOR);			}
"if"				{ return keyword(Tokens.IF);			}
"else"				{ return keyword(Tokens.ELSE);			}
"return"			{ return keyword(Tokens.RETURN);		}
"break"				{ return keyword(Tokens.BREAK);			}
"Print"				{ return keyword(Tokens.PRINT);			}
"ReadInteger"		{ return keyword(Tokens.READ_INTEGER);	}
"ReadLine"			{ return keyword(Tokens.READ_LINE);		}
"static"			{ return keyword(Tokens.STATIC);		}
"instanceof"		{ return keyword(Tokens.INSTANCEOF);	}

	// 识别操作符的规则
"<="				{ return operator(Tokens.LESS_EQUAL);	}
">="				{ return operator(Tokens.GREATER_EQUAL);}
"=="				{ return operator(Tokens.EQUAL);		}
"!="				{ return operator(Tokens.NOT_EQUAL);	}
"&&"				{ return operator(Tokens.AND);			}
"||"				{ return operator(Tokens.OR);			}
{SIMPLE_OPERATOR}	{ return operator((int)yycharat(0));	}

	// 识别常数的规则
"true"				{ return boolConst(true);										}
"false"				{ return boolConst(false);										}
{INTEGER}			{ return intConst(yytext());			}
<YYINITIAL>\"		{ sloc = getPos();
					  yybegin(S);
					  buffer = new StringBuilder();								    }
<S>{NEWLINE}		{ issueError(new NewlineInStrError(sloc, MiscUtils.quote(buffer.toString())));}
<S><<EOF>>			{ issueError(new UntermStrError(sloc, MiscUtils.quote(buffer.toString())));
					  yybegin(YYINITIAL);											}
<S>\"				{ yybegin(YYINITIAL);
					  return StringConst(buffer.toString(), sloc);						}
<S>"\\n"			{ buffer.append('\n');											}
<S>"\\t"			{ buffer.append('\t'); 											}
<S>"\\\""			{ buffer.append('"');											}
<S>"\\\\"			{ buffer.append('\\'); 											}
<S>.				{ buffer.append(yytext()); 										}

	// 识别标识符的规则
{IDENTIFIER}		{ return identifier(yytext());			}
	
	// 上面规则不能识别的字符怎么处理
.					{ issueError(new UnrecogCharError(getPos(), yycharat(0))); 		}
