
parser grammar Type;

options{
    tokenVocab=TestLexer;
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

//Declarations

//Statements

//Expressions

//Top-level blocks

typeDeclaration : TYPE Identifier typeImplement?  typeBody  ;

typeImplement : IMPLEMENTS Identifier (',' Identifier)*;

typeBody : interfaceBlock  containsBlock? attributesBlock? methodBlock? ;

block : '{' statement+ '}' ;

interfaceBlock : '{' methodSignature+  '}' ;

containsBlock : 'contains' '{' (containsDeclaration+ )  '}' ;

attributesBlock : 'attributes'  '{' (declaration SEMI)*  '}' ;

methodBlock : 'methods' LBRACE methodDeclaration+ RBRACE ;

statement : assignment SEMI|declaration SEMI|for|block ;

assignment : fieldAccess ASSIGN expression ;


expression: literals
          | fieldAccess
          | Identifier LPAREN (expression (COMMA expression)*)? RPAREN
          | Identifier (INC | DEC)
          | (INC | DEC) Identifier
          | BANG expression
          | expression MUL expression
          | expression DIV expression
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
          |  '(' expression ')'
          ;

declaration: variableDeclaration | type assignment;

methodSignature : ownMethodSignature ';'| containsMethodSignature ';';

ownMethodSignature : methodType methodName '(' variableList? ')' ;

containsMethodSignature : methodType methodName '(' variableList? ')' 'from' Identifier '.' methodName '(' variableList? ')' ;

methodName : Identifier ;

variableList : variable (',' variable)*   ;

variable :type? variableId ('=' initVariable)? ;

initVariable : expression ;

variableId : Identifier ;

variableDeclaration : variableList;

containsDeclaration : compositeDeclaration | aggregateDeclaration ;

compositeDeclaration :  variableId '=' Identifier '.' 'new' '(' variableList? ')' ';';

aggregateDeclaration : variableId '=' Identifier '(' variableList? ')' ';';

methodDeclaration : methodType Identifier '(' variableList? ')' methodBody  ;

methodBody : '{' statement+ returnStatement?'}' ;

returnStatement : 'return' (expression) ';' ;

methodCall : type '.' Identifier '(' variableList? ')' ';';

fieldAccess :  Identifier (DOT Identifier)*;

for: FOR LPAREN declaration SEMI expression SEMI expression RPAREN statement;


