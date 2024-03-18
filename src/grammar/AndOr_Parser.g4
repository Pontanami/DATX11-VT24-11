parser grammar AndOr_Parser;

options { tokenVocab=TestLexer; }

andExpression
    : equalityExpression
    | andExpression '&' equalityExpression
    ;

equalityExpression
    : BooleanLiteral
    | equalityExpression '==' equalityExpression
    | equalityExpression '!=' equalityExpression
    ;



/*
equalityExpression
    : relationalExpression
    | equalityExpression '==' relationalExpression
    | equalityExpression '!=' relationalExpression
    ;

relationalExpression
    : shiftExpression
    | relationalExpression '<' shiftExpression
    | relationalExpression '>' shiftExpression
    | relationalExpression '<=' shiftExpression
    | relationalExpression '>=' shiftExpression
  //  | relationalExpression 'instanceof' (referenceType | pattern)
    ;

shiftExpression
    : additiveExpression
    | shiftExpression '<' '<' additiveExpression
    | shiftExpression '>' '>' additiveExpression
    | shiftExpression '>' '>' '>' additiveExpression
    ;

additiveExpression
    : multiplicativeExpression
    | additiveExpression '+' multiplicativeExpression
    | additiveExpression '-' multiplicativeExpression
    ;

multiplicativeExpression
    : unaryExpression
    | multiplicativeExpression '*' unaryExpression
    | multiplicativeExpression '/' unaryExpression
    | multiplicativeExpression '%' unaryExpression
    ;


unaryExpression
    : 'int'
    | 'float'
    ;
*/

