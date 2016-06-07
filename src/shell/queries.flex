package shell.parser;

import beaver.Symbol;
import shell.Shell;
import beaver.Scanner;
import shell.parser.TCMQueryParser.Terminals;
import shell.parser.QueryScanException;

%%

%public
%class TCMQueryScanner
%extends Scanner
%function nextToken
%type Symbol
%yylexthrow Scanner.Exception
%line
%column

%{
    String string;
    String error;
%}

digit = [0-9]
number = ({digit})+
newLine = \r|\n|\r\n
whitespace = {newLine}|[ \t\f]
stringDelimiter = \"

stringChars = [^\"]

anyChar = .+

%state STRING
%state ERROR

%%


<YYINITIAL> {
    /*Keywords*/
    "CREATE"         {return new Symbol(Terminals.CREATE);}
    "QUERY"         {return new Symbol(Terminals.QUERY);}
    "BENCHMARK"         {return new Symbol(Terminals.BENCHMARK);}
    "SHOWSUMMARY"         {return new Symbol(Terminals.SHOWSUMMARY);}

    "EDGE"         {return new Symbol(Terminals.EDGE);}
    "NODE"         {return new Symbol(Terminals.NODE);}
    "PATH"         {return new Symbol(Terminals.PATH);}
    "SUBGRAPH"         {return new Symbol(Terminals.SUBGRAPH);}

    "DIR_IN"        {return new Symbol(Terminals.DIR_IN);}
    "DIR_OUT"        {return new Symbol(Terminals.DIR_OUT);}
    "DIR_UNI"        {return new Symbol(Terminals.DIR_UNI);}

    /*separators*/
    "("        {return new Symbol(Terminals.PAR_OPEN);}
    ")"        {return new Symbol(Terminals.PAR_CLOSE);}
    ","        {return new Symbol(Terminals.COMMA);}
    ";"        {return new Symbol(Terminals.SEMICOLON);}
    "{"        {return new Symbol(Terminals.BRACE_OPEN);}
    "}"        {return new Symbol(Terminals.BRACE_CLOSE);}


    {stringDelimiter}   { string = ""; yybegin(STRING); }
    {number}            {return new Symbol(Terminals.NUMBER, Integer.parseInt(yytext()));}
    {whitespace}        { /* ignore */ }
}

<STRING> {
    {stringDelimiter}   { yybegin(YYINITIAL);
                        return new Symbol(Terminals.STRING, string); }
    {stringChars}       { string += yytext(); }
}

[^]                     { /*Syntax error*/ throw new Error("Illegal character <"+yytext()+">"); }