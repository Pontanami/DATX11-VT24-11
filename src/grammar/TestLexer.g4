
lexer grammar TestLexer;

//Types
INT : 'int' ;
FLOAT : 'float' ;
BOOLEAN : 'boolean';
CHAR : 'char' ;
STRING : 'string' ;

BOOLEANLITERAL: 'true' | 'false';
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

// Skip gör så att parsern inte bryr sig om dessa. På så vis kan man skriva fler rader i terminalen som samma statement.
// TODO: Lägg in ; för att avsluta ett statement.
NEWLINE : [\r\n]+ -> skip;
WHITESPACE  : [ \t\r\n\u000C]+ -> skip ;
