grammar SimpleExpr;

// program
// * : one or more
prog : stat* EOF;

// statement
// 'if' : literal
stat : expr SEMI
    | ID ASSIGN expr SEMI
    | FLOAT ASSIGN expr SEMI
    | IF expr SEMI
    ;

// expression
// the expr above has higher priority than below
expr : expr (MUL | DIV) expr
     | expr (ADD | SUB ) expr
     | ID
     | INT
     | FLOAT
     ;

// 自动生成
SEMI : ';' ;
ASSIGN : '=' ;
IF : 'if' ;
MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;


// identifier
ID : (LETTER | '_') (LETTER | DIGIT | '_')*  ;

// number
INT : '0' | ([1-9] [0-9]*) ;

// float
FLOAT : ('0' | ([1-9] [0-9]*))* '.' ([0-9]*);

// 这样实现是多行注释，直到匹配到最后一个 \n 为止 (贪婪匹配)
// SL_COMMENT : '//' .* '\n'  -> skip;

// 单行注释
SL_COMMENT : '//' .*? ('\n' | EOF)  -> skip;

// 多行注释
ML_COMMENT : '/*' .*? '*/'  -> skip;

WS : [ \t\r\n]+ -> skip;

fragment LETTER : [a-zA-Z] ;
fragment DIGIT : [0-9] ;