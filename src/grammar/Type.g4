
parser grammar Type;

options{
    tokenVocab=TestLexer;
}

program : typeDeclaration EOF ;

literals
    : NUMBER | DECIMALNUMBER | BooleanLiteral | StringLiteral ;

numericType
    : 'int'
    | 'float'
    ;

primitiveType
    : numericType
    | 'boolean'
    | 'string'
    ;

assignmentOperator
    : '='
    ;

typeDeclaration : 'type' Identifier typeImplement?  typeBody  ;

typeImplement : 'implements' Identifier (',' Identifier)*;

typeBody : interfaceBlock  containsBlock? attributesBlock? block? ;

block : Identifier? '{' (statement+ | methodDeclaration+) '}' ;

interfaceBlock : '{' methodSignature+  '}' ;

containsBlock : 'contains' '{' (containsDeclaration+ )  '}' ;

attributesBlock : 'attributes'  '{' (variableDeclaration+ )  '}' ;

statement : assignment|declaration|methodCall ;

assignment : leftHandSide assignmentOperator expression ';' ;

leftHandSide : Identifier;

expression:   expression ('*'|'/') expression
    |   expression ('+'|'-') expression
    |   expression ('=='|'!=') expression
    |  literals
    | Identifier
    |   '(' expression ')'
    ;

declaration: variableDeclaration | type assignment;

methodSignature : ownMethodSignature ';'| containsMethodSignature ';';

ownMethodSignature : methodType methodName '(' variableList? ')' ;

containsMethodSignature : methodType methodName '(' variableList? ')' 'from' Identifier '.' methodName '(' variableList? ')' ;

type: Identifier | primitiveType;

methodType : type | 'void' ;

methodName : Identifier ;

variableList : variable (',' variable)*   ;

variable :type? variableId ('=' initVariable)? ;

initVariable : expression ;

variableId : Identifier ;

variableDeclaration : variableList ';'  ;

containsDeclaration : compositeDeclaration | aggregateDeclaration ;

compositeDeclaration :  variableId '=' Identifier '.' 'new' '(' variableList? ')' ';';

aggregateDeclaration : variableId '=' Identifier '(' variableList? ')' ';';

methodDeclaration : methodType Identifier '(' variableList? ')' methodBody  ;

methodBody : '{' statement+ returnStatement?'}' ;

returnStatement : 'return' (expression) ';' ;

methodCall : type '.' Identifier '(' variableList? ')' ';';
