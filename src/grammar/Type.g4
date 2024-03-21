
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

typeBody : interfaceBlock  containsBlock? attributesBlock? methodBlock? ;

block : '{' statement+ '}' ;

interfaceBlock : '{' methodSignature+  '}' ;

containsBlock : 'contains' '{' (containsDeclaration+ )  '}' ;

attributesBlock : 'attributes'  '{' (variableDeclaration+ )  '}' ;

methodBlock : 'methods' LBRACE methodDeclaration+ RBRACE ;

statement : assignment|declaration|methodCall|for|block ;

assignment : leftHandSide assignmentOperator expression ;

leftHandSide : Identifier;

expression: literals
          | Identifier
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

for: FOR LPAREN declaration SEMI expression SEMI expression RPAREN statement;


