
parser grammar TestParser;

options { tokenVocab=TestLexer; }

prog: (expression NEWLINE)* | (declaration NEWLINE)* | (assignment NEWLINE)* ;

type: primitiveType | WORD;

declaration: type WHITESPACE WORD | assignment;

expression:   expression ('*'|'/') expression
    |   expression ('+'|'-') expression
    |  literals
    | WORD
    |   '(' expression ')'
    ;

literals:
NUMBER | DECIMALNUMBER ;


numericType
    : 'int'
    | 'float'
    |
    ;

primitiveType
    : numericType | 'boolean'
    ;

assignmentOperator
    : '='
    ;

assignment
    : leftHandSide assignmentOperator WHITESPACE* expression
    ;

leftHandSide
    : type WHITESPACE WORD WHITESPACE*;

