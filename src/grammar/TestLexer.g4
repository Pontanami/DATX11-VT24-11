
lexer grammar TestLexer;

//Types
INT : 'int' ;
FLOAT : 'float' ;
BOOLEAN : 'boolean';
CHAR : 'char' ;
STRING : 'string' ;

BooleanLiteral: 'true' | 'false';
//Statements
IF         : 'if';
FOR        : 'for';

WORD  : (LETTER | '_')+ ;
NUMBER : DIGIT+ ;
DECIMALNUMBER : DIGIT+ [.] DIGIT+ ;

fragment DIGIT : [0-9] ;
fragment LETTER : [a-z,A-Z] ;



// Operators
ASSIGN   : '=';
GT       : '>';
LT       : '<';
BANG     : '!';
TILDE    : '~';
QUESTION : '?';
COLON    : ':';
ARROW    : '->';
EQUAL    : '==';
LE       : '<=';
GE       : '>=';
NOTEQUAL : '!=';
AND      : '&&';
OR       : '||';
INC      : '++';
DEC      : '--';
ADD      : '+';
SUB      : '-';
MUL      : '*';
DIV      : '/';
BITAND   : '&';
BITOR    : '|';
CARET    : '^';
MOD      : '%';

// Symbols
LPAREN     : '(';
RPAREN     : ')';
LBRACE     : '{';
RBRACE     : '}';
LBRACK     : '[';
RBRACK     : ']';
SEMI       : ';';
COMMA      : ',';
DOT        : '.';
AT         : '@';
COLONCOLON : '::';

NEWLINE : [\r\n]+ ;
WHITESPACE  : [ \t\r\n\u000C]+ ;
