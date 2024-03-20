parser grammar AssignmentsAndExpressions;

options { tokenVocab=TestLexer; }

// Högsta nivå. Består av minst ett statement varav alla avslutas med ";" och efter det End Of File.
program: statement+ EOF;

// Näst högst nivå. Tänker att statements senare ska kunna bestå av flera saker såsom funktionsanrop.
    // Avslutas alltid med semikolon.
statement: assignment SEMI;

// En sorts statement. Tänker att man senare behöver kräva en typ innan "identifier" här.
assignment: identifier ASSIGN expression;

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

// Den här bör vara färdig i stort.
    // Ett boolean expression skulle senare behöva kunna bestå av comparison expressions och funktioner.
booleanExpression
    : LPAREN booleanExpression RPAREN
    | BOOLEANLITERAL
    | booleanExpression boolOperator booleanExpression
    ;

// Hänvisar till lexern snarare än att skriva in "&&" och "||" direkt här.
boolOperator: AND | OR;

/* TESTER:
a = 2 + 3;
b = true || false;
c = false || ((true || false) && true);
*/