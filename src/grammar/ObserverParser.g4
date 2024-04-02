parser grammar ObserverParser;

options{
    tokenVocab=ObserverLexer;
}

typePublishes : PUBLISHES Identifier (COMMA Identifier)* ;

publishStatement : PUBLISH expression (LPAREN Identifier RPAREN)? SEMI ;
addSubscriberStatement : expression WORD_ADD SUBSCRIBER Identifier DOT Identifier SEMI;
removeSubscriberStatement : expression REMOVE SUBSCRIBER Identifier DOT Identifier SEMI;
