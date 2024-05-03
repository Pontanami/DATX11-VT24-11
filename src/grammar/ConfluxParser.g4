
parser grammar ConfluxParser;

options{
    tokenVocab=ConfluxLexer;
}

program : (typeDeclaration | decoratorDeclaration) EOF ;

//Type rules
literals
    : NUMBER | DECIMALNUMBER | SIGNED_NUMBER | SIGNED_DECIMALNUMBER | BooleanLiteral | StringLiteral ;

numericType
    : INT
    | FLOAT
    | SHORT
    | LONG
    | DOUBLE
    ;

primitiveType
    : numericType
    | BOOLEAN
    ;

type: Identifier | STRING | primitiveType | arrayType;

arrayType : (Identifier | STRING | primitiveType) (LBRACK RBRACK)+ ;

typeId : Identifier ;
decoratorId : Identifier ;
methodType : type | VOID ;
methodId : Identifier ;
variableId : Identifier ;
typeModifier : IMMUTABLE | DECORABLE;

//Declarations --------------------------------------------------------------------------------------------------------
typeDeclaration : typeModifier* TYPE Identifier typeExtend? typePublishes? typeBody  ;

typeExtend : EXTENDS Identifier ( COMMA Identifier)*;

typePublishes : PUBLISHES type (COMMA type)* ;

decoratorDeclaration: DECORATOR decoratorId DECORATES typeId decoratorBody ;

declaration: VAR? type declarationPart (COMMA declarationPart)* ;

declarationPart: Identifier (ASSIGN expression)? ;

methodSignature : methodType methodId LPAREN variableList? RPAREN;

attributeDeclaration : declaration (AS Identifier)? ;


componentsDeclaration : aggregateDeclaration | compositeDeclaration ;

compositeDeclaration :   declaration handlesClause ;

aggregateDeclaration : declarationNoAssign handlesClause ;

declarationNoAssign : type (Identifier (COMMA Identifier)*) ;

handlesClause : (HANDLES (delegateMethod (COMMA delegateMethod)*))? ;

delegateMethod : methodId LPAREN variableList? RPAREN renameMethod? ;

renameMethod : (AS Identifier) ;

constructorDeclaration : Identifier LPAREN variableList? RPAREN LBRACE statement* RBRACE;

methodDeclaration : methodType methodId LPAREN variableList? RPAREN methodBody  ;


variableList : variable (COMMA variable)* ;

variable :type variableId;

parameterList : expression (COMMA expression)* ;

//Top-level blocks ----------------------------------------------------------------------------------------------------
typeBody : interfaceBlock constructorsBlock? componentsBlock? attributesBlock? methodBlock? mainBlock? ;

interfaceBlock : LBRACE (methodSignature SEMI)*  RBRACE ;

componentsBlock : COMPONENTS LBRACE (componentsDeclaration SEMI)*  RBRACE ;

attributesBlock : ATTRIBUTES  LBRACE (attributeDeclaration SEMI)*  RBRACE ;

constructorsBlock : SINGLETON? CONSTRUCTORS LBRACE constructorDeclaration* RBRACE ;

methodBlock : METHODS LBRACE methodDeclaration* RBRACE ;

block : LBRACE statement* RBRACE ;

methodBody : LBRACE statement* RBRACE ;

mainBlock : MAIN LPAREN type Identifier RPAREN LBRACE statement* RBRACE ;

decoratorBody : constructorsBlock? attributesBlock? methodBlock? ;

//Statements -------------------------------------------------------------------------------------------------------

statement : javaStatement
          | observerStatement
          | removeDecoratorStatement
          ;

javaStatement : expression SEMI
              | assignment SEMI
              | declaration SEMI
              | forStatement
              | ifStatement
              | whileStatement
              | switchStatement
              | returnStatement
              | block
              | BREAK SEMI
              | CONTINUE SEMI
              ;

observerStatement : publishStatement
                  | addSubscriberStatement
                  | removeSubscriberStatement
                  ;

assignment : assignmentLeftHandSide ASSIGN expression ;

assignmentLeftHandSide : arrayAccess | qualifiedIdentifier ;

returnStatement : RETURN (expression) SEMI ;

forStatement: FOR LPAREN declaration SEMI expression SEMI expression RPAREN statement;

ifStatement : (IF LPAREN expression RPAREN statement) elseIfStatememt* elseStatement? ;

elseIfStatememt : ELIF LPAREN expression RPAREN statement ;

elseStatement : ELSE statement ;

whileStatement : WHILE LPAREN expression RPAREN statement ;

switchStatement : SWITCH LPAREN expression RPAREN LBRACE case* default? RBRACE;

case : CASE expression COLON statement* ;

default : DEFAULT COLON statement* ;

publishStatement : PUBLISH expression explicitEventTypes? SEMI ;

addSubscriberStatement : publisherExpression ADD SUBSCRIBER subscriberExpression
                         COLONCOLON subscriberCallback explicitEventTypes? SEMI;

removeSubscriberStatement : publisherExpression REMOVE SUBSCRIBER subscriberExpression
                            COLONCOLON subscriberCallback explicitEventTypes? SEMI;

explicitEventTypes : LPAREN type (COMMA type)* RPAREN ;
publisherExpression : referenceExpression ;
subscriberExpression : referenceExpression ;
subscriberCallback : Identifier ;

removeDecoratorStatement : decoratedObject REMOVE DECORATOR decoratorRef SEMI;

decoratedObject : referenceExpression ;
decoratorRef : referenceExpression ;

//Expressions -------------------------------------------------------------------------------------------------------
expression: LPAREN expression RPAREN
          | literals
          | qualifiedIdentifier
          | methodChain
          | baseCall
          | addDecoratorExpression
          | arrayConstructor
          | arrayAccess
          | qualifiedIdentifier (INC | DEC)
          | (INC | DEC) qualifiedIdentifier
          | THIS
          | BANG expression
          | <assoc=right> expression CARET expression
          | expression MUL expression
          | expression DIV expression
          | expression MOD expression
          | expression PLUS expression
          | expression MINUS expression
          | expression LT expression
          | expression LE expression
          | expression GT expression
          | expression GE expression
          | expression EQUAL expression
          | expression NOTEQUAL expression
          | expression AND expression
          | expression OR expression
          ;

arrayConstructor : arrayType DOT Identifier LPAREN parameterList? RPAREN ;

arrayAccess : qualifiedIdentifier (LBRACK expression RBRACK)+ ;

qualifiedIdentifier : (THIS DOT)? Identifier (DOT Identifier)*;

methodChain : (baseCall | methodCall) (DOT methodCall)* ;

methodCall : qualifiedIdentifier LPAREN parameterList? RPAREN;

baseCall : BASE DOT Identifier LPAREN parameterList? RPAREN;

addDecoratorExpression : decoratedObject ADD DECORATOR decoratorId DOT methodId LPAREN parameterList? RPAREN ;

referenceExpression : methodChain | arrayAccess | qualifiedIdentifier ;
