// 2022-11-21 class code
/**
 * Simple statically-typed programming language with functions and variables
 * taken from the "Language Implementation Patterns" book.
 *
 * @see http://media.pragprog.com/titles/tpantlr2/code/examples/Cymbol.g4
 */

grammar Cymbol;

@header {
package cymbol;
}

prog : (varDecl | functionDecl)* EOF ;

// '?' means optional
varDecl : type ID ('=' expr)? ';' ;

type : 'int' | 'double' | 'void' ;

// 函数定义（带函数体）
functionDecl : type ID '(' formalParameters? ')' block ;

// 形参列表
formalParameters : formalParameter (',' formalParameter)* ;

// 形参
formalParameter : type ID ;

// 函数体
block : '{' stat* '}' ;

// 语句
stat : block    // block可以嵌套
     | varDecl
     | 'if' expr 'then' stat ('else' stat)?
     | 'return' expr? ';'
     | expr '=' expr ';'
     | expr ';'
     ;

// 表达式
//expr: ID '(' exprList? ')' # Call // function call  e.g. f()
//    | expr '[' expr ']'     // array[i], array[i][j]...
//    | '-' expr
//    | '!' expr
//    | <assoc = right> expr '^' expr // 2 ^ 3 ^ 4
//    | expr (op = '*'| op = '/') expr #MulDiv
//    | expr ('+'| '-') expr
//    | expr ('==' | '!=') expr
//    | '(' expr ')'
//    | ID
//    | INT
//    ;

// 实参列表
exprList : expr (',' expr)* ;

expr: ID '(' exprList? ')'    # Call // function call
    | expr '[' expr ']'       # Index // array subscripts
    | op = '-' expr                # Negate // right association
    | op = '!' expr                # Not // right association
    | <assoc = right> expr '^' expr # Power
    | lhs = expr (op = '*'| op = '/') rhs = expr     # MultDiv
    | lhs = expr (op = '+'| op = '-') rhs = expr     # AddSub
    | lhs = expr (op = '==' | op = '!=') rhs = expr  # EQNE
    | '(' expr ')'            # Parens
    | ID                      # Id
    | INT                     # Int
    ;
////////////////////////////////////////////
// You can use "Alt + Insert" to automatically generate
// the following lexer rules for literals in the grammar above.
// Remember to check and modify them if necessary.

SEMI : ';' ;
COMMA : ',' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACK : '[' ;
RBRACK : ']' ;
LBRACE : '{' ;
RBRACE : '}' ;

IF : 'if' ;
THEN : 'then' ;
ELSE : 'else' ;
RETURN : 'return' ;

INTTYPE : 'int' ;
DOUBLETYPE : 'double' ;
VOIDTYPE : 'void' ;

ADD : '+' ;
SUB : '-' ;
MUL : '*' ;
DIV : '/' ;

EQ : '=' ;
NE : '!=' ;
EE : '==' ;
////////////////////////////////////////////
WS  : [ \t\n\r]+ -> skip ;

ID : LETTER (LETTER | DIGIT)* ;
INT : DIGIT+ ;

fragment LETTER : [a-zA-Z] ;
fragment DIGIT : [0-9] ;