
parser grammar TestParser;

options { tokenVocab=TestLexer; }



prog: (expr NEWLINE)* | (statement NEWLINE)* ;

type: numericType | WORD;

statement: type WHITESPACE WORD ;

expr:   expr ('*'|'/') expr
    |   expr ('+'|'-') expr
    |   NUMBER
    |   DECIMALNUMBER
    |   '(' expr ')'
    ;


numericType
    : integralType
    | floatingPointType
    ;

integralType:
'int' ;

floatingPointType:
'float' ;

assignmentOperator
    : '='
    ;

assignment
    : leftHandSide assignmentOperator expr
    ;

leftHandSide
    : WORD ;

