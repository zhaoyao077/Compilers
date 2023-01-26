// rewrite lab2 code
parser grammar SysYParser;

options {
    tokenVocab = SysYLexer;
}

program
   : compUnit
   ;
compUnit
   : (funcDef | decl)+ EOF
   ;

decl : constDecl | varDecl ;

constDecl
    : CONST bType constDef ( COMMA constDef )* SEMICOLON
    ;

bType
    : INT
    ;

constDef
    : IDENT ( L_BRACKT constExp R_BRACKT )* ASSIGN constInitVal
    ;

constInitVal
    : constExp
    | L_BRACE ( constInitVal ( COMMA constInitVal )* )? R_BRACE
    ;

varDecl
    : bType varDef ( COMMA varDef )* SEMICOLON
    ;

varDef
    : IDENT ( L_BRACKT constExp R_BRACKT )*
    | IDENT ( L_BRACKT constExp R_BRACKT )* ASSIGN initVal ;

initVal
    : exp
    | L_BRACE ( initVal ( COMMA initVal )* )? R_BRACE
    ;


exp
   : L_PAREN exp R_PAREN                   #Parens
   | lVal                                  #LValExp
   | number                                #Num
   | IDENT L_PAREN funcRParams? R_PAREN    #Call
   | unaryOp exp                           #UnaryOpExp
   | exp (MUL | DIV | MOD) exp             #MulDivModExp
   | exp (PLUS | MINUS) exp                #AddSubExp
   | IDENT                                 #Id
   ;

cond
   : exp
   | cond (LT | GT | LE | GE) cond
   | cond (EQ | NEQ) cond
   | cond AND cond
   | cond OR cond
   ;

lVal
   : IDENT (L_BRACKT exp R_BRACKT)*
   ;

number
   : INTEGR_CONST
   ;

unaryOp
   : PLUS
   | MINUS
   | NOT
   ;

funcRParams
   : param (COMMA param)*
   ;

param
   : exp
   ;

constExp
   : exp
   ;




//变量声明

//变量定义


//变量初值


//函数定义
funcDef : funcType IDENT L_PAREN (funcFParams)? R_PAREN funBlock ;

//函数类型
funcType : VOID | INT ;

//函数形参表
funcFParams : funcFParam ( COMMA funcFParam )* ;

//函数形参
funcFParam
    : bType IDENT ('['']'('[' exp ']')*)?
    ;

//函数体
funBlock: L_BRACE ( blockItem )* R_BRACE ;

//语句块
block : L_BRACE ( blockItem )* R_BRACE ;

//语句块项
blockItem : decl | stmt ;

//语句
stmt : lVal ASSIGN exp SEMICOLON                    #AssignStmt
     | (exp)? SEMICOLON                             #ExpStmt
     | block                                        #BlockStmt
     | IF L_PAREN cond R_PAREN stmt ( ELSE stmt )?  #IfStmt
     | WHILE L_PAREN cond R_PAREN stmt              #WhileStmt
     | BREAK SEMICOLON                              #BreakStmt
     | CONTINUE SEMICOLON                           #ContinueStmt
     | RETURN (exp)? SEMICOLON                      #ReturnStmt
     ;
