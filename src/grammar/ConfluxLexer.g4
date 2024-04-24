
lexer grammar ConfluxLexer;

//Types
INT : 'int' ;
FLOAT : 'float' ;
BOOLEAN : 'boolean';
CHAR : 'char' ;
STRING : 'String' ;

//Keywords
TYPE : 'type' ;
EXTENDS : 'extends' ;
IMMUTABLE : 'immutable' ;
COMPONENTS : 'components' ;
FROM : 'from' ;
VOID : 'void' ;
ATTRIBUTES : 'attributes' ;
CONSTRUCTORS : 'constructors' ;
MAIN : 'main' ;
METHODS : 'methods' ;
RETURN : 'return' ;
DEFAULT : 'default' ;
SINGLETON : 'singleton' ;
DECORATOR : 'decorator' ;
DECORATES : 'decorates' ;
BASE : 'base' ;
HANDLES : 'handles' ;
AS : 'as' ;
VAR : 'var' ;

PUBLISHES : 'publishes' ;
PUBLISH : 'publish' ;
SUBSCRIBER : 'subscriber' ;
ADD : 'add' ;
REMOVE : 'remove' ;

//Modifiers
PUBLIC : 'public' ;
PRIVATE : 'private' ;


//Statements
IF         : 'if';
FOR        : 'for';
ELIF       : 'else if';
ELSE       : 'else';
WHILE      : 'while';
SWITCH     : 'switch';
CASE       : 'case';
BREAK      : 'break';

NUMBER : SIGN? DIGIT+ ;
DECIMALNUMBER : SIGN? DIGIT+ [.] DIGIT+ ;

fragment SIGN: [+-];
fragment DIGIT : [0-9] ;
fragment LETTER : [a-zA-Z] ;
BooleanLiteral: 'true' | 'false';
StringLiteral: '"' StringCharacters? '"';

fragment StringCharacters: StringCharacter+;

fragment StringCharacter: ~["\\\r\n] | EscapeSequence;

// §3.10.6 Escape Sequences for Character and String Literals

fragment EscapeSequence: '\\' [btnfr"'\\];

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
PLUS     : '+';
MINUS    : '-';
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

Identifier: IdentifierStart IdentifierPart*;

fragment IdentifierStart:
    [\u0024]
    | [\u0041-\u005A]
    | [\u0061-\u007A]
    ;
fragment IdentifierPart:
    IdentifierStart
    | [\u0030-\u0039]
    | [\u007F-\u009F]
    | [\u00AD]
    | [\u005F]
    | [\u0061-\u007A]
    ;

WORD  : (LETTER | '_')+ ;
NEWLINE : [\r\n]+ -> skip;
WHITESPACE  : [ \t\r\n\u000C]+ -> skip;

COMMENT: '/*' .*? '*/' -> channel(HIDDEN);

LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);

