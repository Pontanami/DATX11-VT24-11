
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
methodName : Identifier ;
variableId : Identifier ;

//Declarations
typeDeclaration : TYPE Identifier typeExtend?  typeBody  ;

typeExtend : EXTENDS Identifier (',' Identifier)*;

declaration: variableDeclaration | type assignment;

methodSignature : ownMethodSignature ';'| containsMethodSignature ';';

ownMethodSignature : methodType methodName '(' variableList? ')' ;

containsMethodSignature : methodType methodName '(' variableList? ')' 'from' Identifier '.' methodName '(' variableList? ')' ;

variableDeclaration : variableList;

containsDeclaration : compositeDeclaration | aggregateDeclaration ;

compositeDeclaration :  variableId '=' Identifier '.' 'new' '(' variableList? ')' ';';

aggregateDeclaration : variableId '=' Identifier '(' variableList? ')' ';';

methodDeclaration : methodType Identifier '(' variableList? ')' methodBody  ;

//Statements ------------------------------------------------------------------------------------------------
statement : assignment SEMI|declaration SEMI| forStatement | ifStatement | block ;

assignment : fieldAccess ASSIGN expression ;

returnStatement : 'return' (expression) ';' ;

forStatement: FOR LPAREN declaration SEMI expression SEMI expression RPAREN statement;

ifStatement : IF LPAREN expression RPAREN statement ;

//Expressions
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

fieldAccess :  Identifier (DOT Identifier)*;

methodCall : type '.' Identifier '(' variableList? ')' ';';

//Top-level blocks
typeBody : interfaceBlock  containsBlock? attributesBlock? methodBlock? ;

interfaceBlock : '{' methodSignature+  '}' ;

containsBlock : 'contains' '{' (containsDeclaration+ )  '}' ;

attributesBlock : 'attributes'  '{' (declaration SEMI)*  '}' ;

methodBlock : 'methods' LBRACE methodDeclaration+ RBRACE ;

block : '{' statement* '}' ;

methodBody : '{' statement+ returnStatement?'}' ;


//TODO Fixa så att vi separerar parameter lista och variabel lista

variableList : variable (',' variable)*   ;

variable :type? variableId ('=' initVariable)? ;

initVariable : expression ;












