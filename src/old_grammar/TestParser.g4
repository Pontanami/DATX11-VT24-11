
//Lite syntax för g4:
// * betyder att regeln kan upprepas flera gånger
// ? betyder att regeln är 'non-greedy'  "the previous sub-rule matches everything except what follows it"

parser grammar TestParser;

options { tokenVocab=TestLexer; }

prog: (expression NEWLINE)* | (declaration ';' NEWLINE)* | (assignment ';' NEWLINE)* | (statement NEWLINE)*  ;

type: primitiveType | WORD;

declaration: type WHITESPACE WORD;

expression:   expression ('*'|'/') expression
    |   expression ('+'|'-') expression
    |   expression ('=='|'!=') expression
    |  literals
    | WORD
    |   '(' expression ')'
    ;
literals:
NUMBER | DECIMALNUMBER | BooleanLiteral ;


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

comparisonOperator
    : '<'
    | '>'
    | '>='
    | '<='
    ;

assignment
    : leftHandSide assignmentOperator WHITESPACE* expression
    ;

leftHandSide
    : type WHITESPACE WORD WHITESPACE*;

statement
    : ifStatement
    | assignment
    ;

ifStatement
    : IF '(' expression ')'
    ;

block
    : '{' (statement NEWLINE)* '}'
    ;

