parser grammar AndOr_Parser;

options { tokenVocab=TestLexer; }

/*TODO: Lägg in denna under en "expression" rule tillsammans med alla andra typer av expressions.
        e.g. functionCall, addExpression, mulExpression etc.*/
booleanExpression
    : LPAREN booleanExpression RPAREN EOF // Utan EOF så accepterar den t.ex. "(true) true)"
    | BOOLEANLITERAL
    | booleanExpression boolOperator booleanExpression
    ;

boolOperator: AND | OR;

/* TESTER:
(true && ((true || false) && false)) || false && (true && ((true || false) && false))

((((((((true) || false) && true) || false) && true) || false) && true) || false)

((true)

(true) true)
*/