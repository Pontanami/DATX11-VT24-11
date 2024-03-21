//Lite syntax för g4:
// * betyder att regeln kan upprepas flera gånger
// ? betyder att regeln är 'non-greedy'  "the previous sub-rule matches everything except what follows it"

parser grammar ArithmeticOperations;

options { tokenVocab=TestLexer; }

arithmaticAssignment:
numericType variableName assignment arithmethicExpression ;

arithmethicExpression
    : term (arithmeticOperation arithmethicExpression)*
    | LPAREN arithmethicExpression RPAREN
    | term
    ;

assignment: ASSIGN ;

term: numbers | variableName ;

numbers: NUMBER | DECIMALNUMBER ;
variableName: WORD(WORD | NUMBER)* ;


arithmeticOperation
    : ADD
    | SUB
    | MUL
    | DIV
    | CARET
    | MOD
    ;


numericType
    : 'int'
    | 'float'
    |
    ;

