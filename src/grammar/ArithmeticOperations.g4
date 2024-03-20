//Lite syntax för g4:
// * betyder att regeln kan upprepas flera gånger
// ? betyder att regeln är 'non-greedy'  "the previous sub-rule matches everything except what follows it"

parser grammar ArithmeticOperations;

options { tokenVocab=TestLexer; }

mathExpression
    : intExpression
    | floatExpression
    ;

intExpression
    : INT numbers (arithmeticOperation intExpression)*
    | LPAREN intExpression RPAREN
    | INT numbers
    ;

floatExpression
    : FLOAT numbers (arithmeticOperation intExpression)*
    | LPAREN intExpression RPAREN
    | FLOAT numbers
    ;
numbers: NUMBER | DECIMALNUMBER ;


arithmeticOperation: ADD
    | SUB
    | MUL
    | DIV
    ;

//en numerictype med en checker hade varit bättre, just nu är int och float hårdkodade vilket inte är så bra
numericType: 'int' | 'float' ;



