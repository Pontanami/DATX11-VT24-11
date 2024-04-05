
parser grammar Parser;

options{
    tokenVocab=Lexer;
}

program : typeDeclaration EOF ;

//Type rules
literals
    : NUMBER | DECIMALNUMBER | BooleanLiteral | StringLiteral ;

numericType
    : INT
    | FLOAT
    ;

primitiveType
    : numericType
    | BOOLEAN
    | STRING
    ;

type: Identifier | primitiveType | arrayType;

arrayType : (Identifier|primitiveType) LBRACK RBRACK ;

methodType : type | VOID ;
methodName : Identifier ;
variableId : Identifier ;

constructorModifier : PUBLIC | PRIVATE | SINGLETON ;

//Declarations --------------------------------------------------------------------------------------------------------
typeDeclaration : TYPE Identifier typeExtend?  typeBody  ;

typeExtend : EXTENDS Identifier ( COMMA Identifier)*;

declaration: variableDeclaration | type assignment | arrayDeclaration;

methodSignature : ownMethodSignature SEMI| containsMethodSignature SEMI;

ownMethodSignature : methodType methodName LPAREN variableList? RPAREN ;

containsMethodSignature : methodType methodName LPAREN variableList? RPAREN FROM Identifier DOT methodName LPAREN variableList? RPAREN ;

variableDeclaration : declaredVariableList;

containsDeclaration : compositeDeclaration | aggregateDeclaration ;

compositeDeclaration :  variableId ASSIGN Identifier DOT Identifier LPAREN parameterList? RPAREN SEMI ;

aggregateDeclaration : variableId ASSIGN Identifier LPAREN parameterList? RPAREN SEMI ;

constructorDeclaration : constructorModifier? Identifier LPAREN variableList? RPAREN SEMI;

methodDeclaration : methodType Identifier LPAREN variableList? RPAREN methodBody  ;

arrayDeclaration : arrayType variableId;

//Statements -------------------------------------------------------------------------------------------------------
statement : expression SEMI | assignment SEMI|declaration SEMI| forStatement | ifStatement | whileStatement | switchStatement | returnStatement | block ;

assignment : qualifiedIdentifier ASSIGN (expression | arrayAssignement) ;

arrayAssignement : arrayInitWithLength | arrayInitWithValues ;

arrayInitWithValues : LBRACE literals (COMMA literals)* RBRACE ;

arrayInitWithLength : type LBRACK expression RBRACK ;

returnStatement : RETURN (expression) SEMI ;

forStatement: FOR LPAREN declaration SEMI expression SEMI expression RPAREN statement;

ifStatement : (IF LPAREN expression RPAREN statement) elseIfStatememt* elseStatement? ;

elseIfStatememt : ELIF LPAREN expression RPAREN statement ;

elseStatement : ELSE statement ;

whileStatement : WHILE LPAREN expression RPAREN statement ;

switchStatement : SWITCH LPAREN expression RPAREN LBRACE case* default? RBRACE;

case : (CASE expression COLON statement BREAK SEMI) ;

default : (DEFAULT COLON statement BREAK SEMI) ;


//Expressions -------------------------------------------------------------------------------------------------------
expression: literals
          | qualifiedIdentifier
          | methodCall
          | qualifiedIdentifier LPAREN expression RPAREN
          | qualifiedIdentifier LBRACK expression RBRACK
          | qualifiedIdentifier (INC | DEC)
          | (INC | DEC) qualifiedIdentifier
          | BANG expression
          | <assoc=right> expression CARET expression
          | expression MUL expression
          | expression DIV expression
          | expression MOD expression
          | expression ADD expression
          | expression SUB expression
          | expression LT expression
          | expression LE expression
          | expression GT expression
          | expression GE expression
          | expression EQUAL expression
          | expression NOTEQUAL expression
          | expression AND expression
          | expression OR expression
          ;

qualifiedIdentifier :  Identifier (DOT Identifier)*;

methodCall : qualifiedIdentifier LPAREN parameterList? RPAREN;

//Top-level blocks ----------------------------------------------------------------------------------------------------
typeBody : interfaceBlock  containsBlock? constructorsBlock? attributesBlock? methodBlock? ;

interfaceBlock : LBRACE methodSignature*  RBRACE ;

containsBlock : CONTAINS LBRACE containsDeclaration*  RBRACE ;

attributesBlock : ATTRIBUTES  LBRACE (declaration SEMI)*  RBRACE ;

constructorsBlock : CONSTRUCTORS LBRACE constructorDeclaration* RBRACE ;

methodBlock : METHODS LBRACE methodDeclaration* RBRACE ;

block : LBRACE statement* RBRACE ;

methodBody : LBRACE statement* RBRACE ;

//Var ska de här vara

declaredVariableList : variable (COMMA variable (ASSIGN initVariable)?)* ;

variableList : variable (COMMA variable)*   ;

variable :type variableId;

initVariable : expression ;

parameterList : parameter (COMMA parameter)* ;

parameter : expression ;
