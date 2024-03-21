parser grammar AssignmentsAndExpressions;

options { tokenVocab=TestLexer; }

// Högsta nivå. Består av minst ett statement varav alla avslutas med ";" och efter det End Of File.
program: statement* EOF;

// Näst högst nivå. Tänker att statements senare ska kunna bestå av flera saker såsom funktionsanrop.
    // Avslutas alltid med semikolon.
statement: assignment SEMI;

// En sorts statement.
assignment: type identifier ASSIGN expression;

type: INT | FLOAT | BOOLEAN | CHAR | STRING;

// Finns kanske ett bättre sätt att definiera identifiers...?
identifier: WORD+NUMBER*WORD*;

// Många fler typer av expressions kommer nog behövas.
expression
    : booleanExpression
    | mathExpression
    ;

// La bara in expressions för addition och multiplikation hittills.
mathExpression
    : mathExpression ADD mathExpression
    | mathExpression MUL mathExpression
    | NUMBER
    | DECIMALNUMBER;

//Ett boolean expression skulle senare behöva kunna bestå av funktioner.
booleanExpression
    : LPAREN booleanExpression RPAREN
    | BOOLEANLITERAL
    | booleanExpression boolOperator booleanExpression
    | comparisonExpression
    ;

//Ett specialfall av boolean expression.
comparisonExpression
    : (NUMBER | DECIMALNUMBER) comparisonOperator (NUMBER | DECIMALNUMBER)
    ;

// Möjliga jämförelseoperatorer.
comparisonOperator: EQUAL | NOTEQUAL | LT | GT | LE | GE;

// Hänvisar till lexern snarare än att skriva in "&&" och "||" direkt här.
boolOperator: AND | OR;

/* TESTER:
int a = 2 + 3;
boolean b = true || false;
boolean c = false || ((true || false) && true);

boolean a = 2 > 3;
boolean b = (2 >3) && true;
*/