
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
    ;

assignmentOperator
    : '='
    ;

typeDeclaration : 'type' Identifier typeImplement?  typeBody  ;

typeImplement : 'implements' Identifier ;

typeBody : interfaceBlock  containsBlock? attributesBlock? block? ;

block : Identifier? '{' statement+ '}' ;

interfaceBlock : '{' methodSignature+  '}' ;

containsBlock : 'contains' '{' (compositeDeclaration+ )  '}' ;

attributesBlock : 'attributes'  '{' (variableDeclaration+ )  '}' ;

statement : assignment|declaration ;

assignment : leftHandSide assignmentOperator expression ';' ;

leftHandSide : Identifier;

expression:   expression ('*'|'/') expression
    |   expression ('+'|'-') expression
    |   expression ('=='|'!=') expression
    |  literals
    |   '(' expression ')'
    ;

declaration: variableDeclaration | type assignment;

methodSignature : ownMethodSignature ';'| compositeMethodSignature ';';

ownMethodSignature : methodType methodName '(' variableList? ')' | Identifier ;

compositeMethodSignature : methodType methodName '(' variableList? ')' 'from' Identifier '.' methodName '(' variableList? ')' ;

type: Identifier | primitiveType;

methodType : type | 'void' ;

methodName : Identifier ;

variableList : variable (',' variable)*   ;

variable :type? variableId ('=' initVariable)? ;

initVariable : expression ;

variableId : Identifier ;

variableDeclaration : variableList ';'  ;

compositeDeclaration : variableId '=' Identifier '.' 'new' '(' variableList? ')' ';';