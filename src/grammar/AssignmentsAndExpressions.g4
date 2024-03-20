parser grammar AssignmentsAndExpressions;

options { tokenVocab=TestLexer; }

program: statement+ EOF;

statement: assignment SEMI;

assignment: identifier ASSIGN expression;

identifier: WORD+NUMBER*WORD*;

expression
    : booleanExpression
    | mathExpression
    ;

mathExpression
    : mathExpression ADD mathExpression
    | mathExpression MUL mathExpression
    | NUMBER
    | DECIMALNUMBER;

booleanExpression
    : LPAREN booleanExpression RPAREN
    | BOOLEANLITERAL
    | booleanExpression boolOperator booleanExpression
    ;

boolOperator: AND | OR;

/* TESTER:
a = 2 + 3;
b = true || false;
c = false || ((true || false) && true);
*/