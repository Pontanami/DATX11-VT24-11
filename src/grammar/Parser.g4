
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

type: Identifier | primitiveType;

methodType : type | VOID ;
methodName : Identifier ;
variableId : Identifier ;

//Declarations --------------------------------------------------------------------------------------------------------
typeDeclaration : TYPE Identifier typeExtend?  typeBody  ;

typeExtend : EXTENDS Identifier ( COMMA Identifier)*;

declaration: variableDeclaration | type assignment;

methodSignature : ownMethodSignature SEMI| containsMethodSignature SEMI;

ownMethodSignature : methodType methodName LPAREN variableList? RPAREN ;

containsMethodSignature : methodType methodName LPAREN variableList? RPAREN FROM Identifier DOT methodName LPAREN variableList? RPAREN ;

variableDeclaration : variableList;

containsDeclaration : compositeDeclaration | aggregateDeclaration ;

compositeDeclaration :  variableId ASSIGN Identifier DOT NEW LPAREN variableList? RPAREN SEMI;

aggregateDeclaration : variableId ASSIGN Identifier LPAREN variableList? RPAREN SEMI;

methodDeclaration : methodType Identifier LPAREN variableList? RPAREN methodBody  ;

//Statements -------------------------------------------------------------------------------------------------------
statement : assignment SEMI|declaration SEMI| forStatement | ifStatement | block ;

assignment : fieldAccess ASSIGN expression ;

returnStatement : RETURN (expression) SEMI ;

forStatement: FOR LPAREN declaration SEMI expression SEMI expression RPAREN statement;

ifStatement : IF LPAREN expression RPAREN statement ;

//Expressions -------------------------------------------------------------------------------------------------------
expression: literals
          | fieldAccess
          |  LPAREN expression RPAREN
          | Identifier LPAREN (expression (COMMA expression)*)? RPAREN
          | Identifier (INC | DEC)
          | (INC | DEC) Identifier
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
          | Identifier ASSIGN expression
          ;

fieldAccess :  Identifier (DOT Identifier)*;

methodCall : type DOT Identifier LPAREN variableList? RPAREN SEMI;

//Top-level blocks
typeBody : interfaceBlock  containsBlock? attributesBlock? methodBlock? ;

interfaceBlock : LBRACE methodSignature+  RBRACE ;

containsBlock : CONTAINS LBRACE (containsDeclaration+ )  RBRACE ;

attributesBlock : ATTRIBUTES  LBRACE (declaration SEMI)*  RBRACE ;

methodBlock : METHODS LBRACE methodDeclaration+ RBRACE ;

block : LBRACE statement* RBRACE ;

methodBody : LBRACE statement+ returnStatement? RBRACE ;


//TODO Fixa så att vi separerar parameter lista och variabel lista

variableList : variable (COMMA variable)*   ;

variable :type? variableId (ASSIGN initVariable)? ;

initVariable : expression ;












