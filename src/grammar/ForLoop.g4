parser grammar ForLoop;

options{
    tokenVocab=TestLexer;
}

for: FOR LPAREN declaration SEMI expression SEMI expression RPAREN statement;

statement: expression SEMI
         | declaration SEMI
         | LBRACE statement* RBRACE
         ;

declaration: type declarationVar (COMMA declarationVar)* ;
declarationVar : Identifier (ASSIGN expression)? ;

expression: DECIMALNUMBER
          | NUMBER
          | BooleanLiteral
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
          ;

type: primitiveType | STRING | Identifier ;
primitiveType: INT | FLOAT | BOOLEAN | CHAR ;
