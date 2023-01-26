// import sysy.SysYParser;
// import sysy.SysYParserBaseListener;

import java.util.ArrayList;

public class SymbolTableListener extends SysYParserBaseListener {
  private GlobalScope globalScope = null;
  private Scope currentScope = null;
  private int localScopeCounter = 0;
  /**
   * Building Scope
   */

  /**
   * (1) When/How to start/enter a new scope?
   */
  @Override
  public void enterProgram(SysYParser.ProgramContext ctx) {
    globalScope = new GlobalScope(null);
    currentScope = globalScope;
  }

  @Override
  public void enterFuncDef(SysYParser.FuncDefContext ctx) {
    String typeName = ctx.funcType().getText();
    globalScope.resolve(typeName);

    String funName = ctx.IDENT().getText();
    FunctionSymbol fun = new FunctionSymbol(funName, currentScope, new BasicType(typeName));
    if(globalScope.getSymbols().get(funName) != null){
      //Error 4
      Main.ErrorExist = true;
      System.err.println("Error type 4 at Line " + ctx.IDENT().getSymbol().getLine() + ": Redefined function: " +  ctx.IDENT().getText() + ".");
      return;
    }

    // initialize params
    if(ctx.funcFParams() != null) {
      for (int i = 0; ctx.funcFParams().funcFParam(i) != null; i++) {
        String param = ctx.funcFParams().funcFParam(i).getText();
        Type type = (Type) globalScope.resolve((ctx.funcFParams().funcFParam(i).bType().getText()));
        String name = ctx.funcFParams().funcFParam(i).IDENT().getText();
        int dim = 0;
        for (int j = 0; j < param.length(); j++) {
          if(param.charAt(j) == '['){
            dim ++;
          }
        }
        if(dim == 0){
          fun.paramsType.add(type);
        } else {
          fun.paramsType.add(new ArraySymbol(name, type, dim));
        }
      }
    }

    int lineNo = ctx.IDENT().getSymbol().getLine();
    int column = ctx.IDENT().getSymbol().getCharPositionInLine();
    if(lineNo == Main.lineNo && column == Main.column){
      Main.targetScope = currentScope;
      Main.oldName = funName;
    }
    currentScope.define(fun);
    currentScope.put(funName, lineNo + " " + column,0);

    currentScope = fun;
  }

  @Override
  public void enterBlock(SysYParser.BlockContext ctx) {
    LocalScope localScope = new LocalScope(currentScope);
    String localScopeName = localScope.getName() + localScopeCounter;
    localScope.setName(localScopeName);
    localScopeCounter++;

    currentScope = localScope;
  }

  /**
   * (2) When/How to exit the current scope?
   */
  @Override
  public void exitProgram(SysYParser.ProgramContext ctx) {
    currentScope = currentScope.getEnclosingScope();
  }

  @Override
  public void exitFuncDef(SysYParser.FuncDefContext ctx) {
    if(globalScope.getSymbols().get(ctx.IDENT().getText()) != null){
      currentScope = globalScope;
      return;
    }
    currentScope = currentScope.getEnclosingScope();
  }

  @Override
  public void exitBlock(SysYParser.BlockContext ctx) {
    currentScope = currentScope.getEnclosingScope();
  }

  /**
   * Error Listener
   */


  @Override
  public void exitVarDecl(SysYParser.VarDeclContext ctx) {
    String typeName = ctx.bType().getText();
    Type type = (Type) globalScope.resolve(typeName);

    for (int i = 0; ctx.varDef(i) != null; i++) {
      String varName = ctx.varDef(i).IDENT().getText();
      Symbol varSymbol = null;

      int dim = 0;
      for (int j = 0; j < ctx.varDef(i).getText().length() &&
              (ctx.varDef(i).initVal() == null || !ctx.varDef(i).getText().substring(j).equals(ctx.varDef(i).initVal().getText())); j++) {
        if(ctx.varDef(i).getText().charAt(j) == '['){
          dim++;
        }
      }
      if(dim == 0){
        varSymbol = new VariableSymbol(varName, type);
      } else {
        varSymbol = new ArraySymbol(varName, type, dim);
      }

      if(currentScope == globalScope && globalScope.getSymbols().get(varName) != null
              || currentScope.getSymbols().get(varName) != null){
        //Error 3
        Main.ErrorExist = true;
        System.err.println("Error type 3 at Line " + ctx.varDef(i).IDENT().getSymbol().getLine() + ": Redefined variable: " +  ctx.varDef(i).IDENT().getText() + ".");
        continue;
      }

      int lineNo = ctx.varDef(i).IDENT().getSymbol().getLine();
      int column = ctx.varDef(i).IDENT().getSymbol().getCharPositionInLine();
      if(lineNo == Main.lineNo && column == Main.column){
        Main.targetScope = currentScope;
        Main.oldName = ctx.varDef(i).IDENT().getText();
      }
      currentScope.define(varSymbol);
//      System.out.println(currentScope.getName() + " define : " + varName + " type : " + (varSymbol instanceof ArraySymbol));
      currentScope.put(varName,  lineNo + " " + column, 0);
    }
  }

  @Override
  public void exitConstDecl(SysYParser.ConstDeclContext ctx) {
    String typeName = ctx.bType().getText();
    Type type = (Type) globalScope.resolve(typeName);

    for (int i = 0; ctx.constDef(i) != null; i++) {
      String varName = ctx.constDef(i).IDENT().getText();
      Symbol varSymbol = null;

      int dim = 0;
      for (int j = 0; j < ctx.constDef(i).getText().length(); j++) {
        if(ctx.constDef(i).getText().charAt(j) == '['){
          dim++;
        }
      }
      if(dim == 0){
        varSymbol = new VariableSymbol(varName, type);
      } else {
        varSymbol = new ArraySymbol(varName, type, dim);
      }

      if(currentScope == globalScope && globalScope.getSymbols().get(varName) != null
              || currentScope.getSymbols().get(varName) != null){
        //Error 3
        Main.ErrorExist = true;
        System.err.println("Error type 3 at Line " + ctx.constDef(i).IDENT().getSymbol().getLine() + ": Redefined variable: " +  ctx.constDef(i).IDENT().getText() + ".");
        continue;
      }

      int lineNo = ctx.constDef(i).IDENT().getSymbol().getLine();
      int column = ctx.constDef(i).IDENT().getSymbol().getCharPositionInLine();
      if(lineNo == Main.lineNo && column == Main.column){
        Main.targetScope = currentScope;
        Main.oldName = ctx.constDef(i).IDENT().getText();
      }
      currentScope.define(varSymbol);
      currentScope.put(varName,  lineNo + " " + column,0);
    }
  }

  @Override
  public void enterFuncFParam(SysYParser.FuncFParamContext ctx) {
    String typeName = ctx.bType().getText();
    Type type = (Type) globalScope.resolve(typeName);

    String varName = ctx.IDENT().getText();
    Symbol varSymbol = null;

    int dim = 0;
    for (int j = 0; j < ctx.getText().length(); j++) {
      if(ctx.getText().charAt(j) == '['){
        dim++;
      }
    }
    if(dim == 0){
      varSymbol = new VariableSymbol(varName, type);
    } else {
      varSymbol = new ArraySymbol(varName, type, dim);
    }

    if(currentScope.getSymbols().get(varName) != null){
      //Error 3
      Main.ErrorExist = true;
      System.err.println("Error type 3 at Line " + ctx.IDENT().getSymbol().getLine() + ": Redefined variable: " +  ctx.IDENT().getText() + ".");
      return;
    }

    int lineNo = ctx.IDENT().getSymbol().getLine();
    int column = ctx.IDENT().getSymbol().getCharPositionInLine();
    if(lineNo == Main.lineNo && column == Main.column){
      Main.targetScope = currentScope;
      Main.oldName = ctx.IDENT().getText();
    }
    currentScope.define(varSymbol);
    currentScope.put(varName,  lineNo + " " + column,0);
  }

  @Override
  public void enterLVal(SysYParser.LValContext ctx) {
    if(currentScope.resolve(ctx.IDENT().getText()) == null){
      //Error 1
      Main.ErrorExist = true;
      System.err.println("Error type 1 at Line " + ctx.IDENT().getSymbol().getLine() + ": Undefined variable: " +  ctx.IDENT().getText() + ".");
      return;
    }

    int dim = 0;
    for (int i = 0; ctx.L_BRACKT(i) != null; i++) {
      dim --;
    }
    if(currentScope.resolve(ctx.IDENT().getText()) instanceof ArraySymbol){
      dim += ((ArraySymbol) currentScope.resolve(ctx.IDENT().getText())).getDim();
    }
    if(dim < 0){
      //Error 9
      Main.ErrorExist = true;
      System.err.println("Error type 9 at Line " + ctx.IDENT().getSymbol().getLine() + ": Not an array: " +  ctx.IDENT().getText() + ".");
      return;
    }

    int lineNo = ctx.IDENT().getSymbol().getLine();
    int column = ctx.IDENT().getSymbol().getCharPositionInLine();
    Scope tempScope = currentScope;
    if(lineNo == Main.lineNo && column == Main.column){
      //TODO
      Main.oldName = ctx.IDENT().getText();
      while(tempScope.getSymbols().get(Main.oldName) == null) {
        tempScope = tempScope.getEnclosingScope();
      }
      Main.targetScope = tempScope;
    }
    tempScope.put(ctx.IDENT().getText(),  lineNo + " " + column,1);
  }

  @Override
  public void exitCall(SysYParser.CallContext ctx) {
    if(currentScope.resolve(ctx.IDENT().getText()) == null){
      //Error 2
      Main.ErrorExist = true;
      System.err.println("Error type 2 at Line " + ctx.IDENT().getSymbol().getLine() + ": Undefined function: " +  ctx.IDENT().getText() + ".");
      return;
    }

    if(!(currentScope.resolve(ctx.IDENT().getText()) instanceof FunctionSymbol)){
      //Error 10
      Main.ErrorExist = true;
      System.err.println("Error type 10 at Line " + ctx.IDENT().getSymbol().getLine() + ": Not a function: " + ctx.IDENT().getText() + ".");
      return;
    }

    //TODO: Error 8
    if(  ctx.funcRParams() == null && !((FunctionSymbol)currentScope.resolve(ctx.IDENT().getText())).paramsType.isEmpty()
      || ctx.funcRParams() != null &&  ((FunctionSymbol)currentScope.resolve(ctx.IDENT().getText())).paramsType.isEmpty())
    {
      //Error 8
      Main.ErrorExist = true;
      System.err.println("Error type 8 at Line " + ctx.IDENT().getSymbol().getLine() + ": Function is not applicable for arguments.");
      return;
    }

    if(ctx.funcRParams() == null && ((FunctionSymbol)currentScope.resolve(ctx.IDENT().getText())).paramsType.isEmpty()){
      return;
    }

    int RParamCount = 0;
    int FParamCount = 0;
    while(ctx.funcRParams().param(RParamCount) != null){
      RParamCount ++;
    }
    FParamCount = ((FunctionSymbol)currentScope.resolve(ctx.IDENT().getText())).paramsType.size();
    if(RParamCount != FParamCount){
      //Error 8
      Main.ErrorExist = true;
      System.err.println("Error type 8 at Line " + ctx.IDENT().getSymbol().getLine() + ": Function is not applicable for arguments.");
      return;
    }

    int count = FParamCount;
    ArrayList<Type> FParams = ((FunctionSymbol)currentScope.resolve(ctx.IDENT().getText())).paramsType;
    for (int i = 0; i < count; i++) {
      int FParamDim = 0;
      if(FParams.get(i) instanceof ArraySymbol){
        FParamDim = ((ArraySymbol)FParams.get(i)).getDim();
      }

      int RParamDim = 0;
      String rParam = ctx.funcRParams().param(i).getText();
      StringBuilder id = new StringBuilder();
      for (int j = 0; j < rParam.length(); j++) {
        if(rParam.charAt(j) == '['){
          RParamDim --;
        }
      }
      if(RParamDim == 0){
        id.append(rParam);
      } else {
        for (int j = 0; j < rParam.length(); j++) {
          if(isIDENT(rParam.charAt(j))){
            id.append(rParam.charAt(j));
          } else {
            break;
          }
        }
      }
      if(currentScope.resolve(id.toString()) instanceof FunctionSymbol){
        //Error 8
        Main.ErrorExist = true;
        System.err.println("Error type 8 at Line " + ctx.IDENT().getSymbol().getLine() + ": Function is not applicable for arguments.");
        return;
      }
      if(currentScope.resolve(id.toString()) instanceof ArraySymbol){
        RParamDim += ((ArraySymbol)currentScope.resolve(id.toString())).getDim();
      }

      if(FParamDim != RParamDim){
        //Error 8
        Main.ErrorExist = true;
        System.err.println("Error type 8 at Line " + ctx.IDENT().getSymbol().getLine() + ": Function is not applicable for arguments.");
        return;
      }
    }








    int lineNo = ctx.IDENT().getSymbol().getLine();
    int column = ctx.IDENT().getSymbol().getCharPositionInLine();

    Scope tempScope = currentScope;
    if(lineNo == Main.lineNo && column == Main.column){
      //TODO
      Main.oldName = ctx.IDENT().getText();
      while(tempScope.getSymbols().get(Main.oldName) == null) {
        tempScope = tempScope.getEnclosingScope();
      }
      Main.targetScope = tempScope;
    }
    tempScope.put(ctx.IDENT().getText(),  lineNo + " " + column,1);
  }

  @Override
  public void enterAssignStmt(SysYParser.AssignStmtContext ctx) {
    if(currentScope.resolve(ctx.lVal().IDENT().getText()) instanceof FunctionSymbol){
      //Error 11
      Main.ErrorExist = true;
      System.err.println("Error type 11 at Line " + ctx.lVal().IDENT().getSymbol().getLine() + ": The left-hand side of an assignment must be a variable.");
      return;
    }

    int leftDim = 0;
    Symbol leftSymbol = currentScope.resolve(ctx.lVal().IDENT().getText());

    if(leftSymbol instanceof ArraySymbol){
      leftDim = ((ArraySymbol)leftSymbol).getDim();
    }
    for (int i = 0; i < ctx.lVal().getText().length(); i++) {
      if(ctx.lVal().getText().charAt(i) == '['){
        leftDim --;
      }
    }
//    System.out.println("left: " + leftSymbol.getName() + " dim: " + leftDim);

    int rightDim = 0;
    Symbol rightSymbol = null;
    StringBuilder rightIDENT = new StringBuilder();
    boolean inIdent = true;

    for (int i = 0; i < ctx.exp().getText().length(); i++) {
      if(isOperator(ctx.exp().getText().charAt(i)))
        break;
      if(ctx.exp().getText().charAt(i) == '[')
        rightDim --;
      if(isIDENT(ctx.exp().getText().charAt(i)) && inIdent) {
        rightIDENT.append(ctx.exp().getText().charAt(i));
      } else {
        inIdent = false;
      }
    }
    rightSymbol = currentScope.resolve(rightIDENT.toString());

    if(rightSymbol instanceof ArraySymbol){
      rightDim += ((ArraySymbol) rightSymbol).getDim();
    }
//    System.out.println("right : " + rightIDENT + " dim : " + rightDim);

    if(rightDim >= 0 && leftDim >= 0 && rightDim != leftDim){
      //Error 5
      Main.ErrorExist = true;
      System.err.println("Error type 5 at Line " + ctx.lVal().IDENT().getSymbol().getLine() + ": type.Type mismatched for assignment.");
    }
  }

  private boolean isOperator(char c){
    return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
  }

  private boolean isIDENT(char c){
    return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c >= '0' && c <= '9';
  }

  private String resolveExp(String s){
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      if(isIDENT(s.charAt(i))){
        result.append(s.charAt(i));
      } else if(s.charAt(i) == '(' && s.charAt(i + 1) == ')'){
        return result.append("()").toString();
      } else if(s.charAt(i) == '[') {
        for (int j = i; j < s.length() && !isOperator(s.charAt(j)); j++) {
          if(s.charAt(j) == '['){
            result.append("[]");
          }
        }
        break;
      }
    }

    return result.toString();
  }

  @Override
  public void enterReturnStmt(SysYParser.ReturnStmtContext ctx) {
    if(currentScope instanceof FunctionSymbol && "void".equals(((FunctionSymbol)currentScope).getRetType().getName()) && ctx.exp() != null){
      //Error 7
      Main.ErrorExist = true;
      System.err.println("Error type 7 at Line " + ctx.RETURN().getSymbol().getLine() + ": type.Type mismatched for return.");
      return;
    } else if(ctx.exp() == null){
      return;
    }


    String expr = resolveExp(ctx.exp().getText());

//    System.out.println("expr : " + expr  + ".");
    if(currentScope.resolve(expr) instanceof FunctionSymbol || currentScope.resolve(expr) instanceof ArraySymbol){
      //Error 7
      Main.ErrorExist = true;
      System.err.println("Error type 7 at Line " + ctx.RETURN().getSymbol().getLine() + ": type.Type mismatched for return.");
      return;
    }

    String varName = "";
    int dim = 0;
    for (int i = 0; i < expr.length(); i++) {
      if(!isIDENT(expr.charAt(i) )){
        varName = expr.substring(0,i);
        break;
      }
    }

    if(currentScope.resolve(varName) instanceof ArraySymbol){
      dim += ((ArraySymbol) currentScope.resolve(varName)).getDim();
    }
    for (int i = 0; i < expr.length(); i++) {
      if(expr.charAt(i) == '['){
        dim --;
      }
    }

//    System.out.println("varName : " + varName  + ".");
    if(dim > 0){
      //Error 7
      Main.ErrorExist = true;
      System.err.println("Error type 7 at Line " + ctx.RETURN().getSymbol().getLine() + ": type.Type mismatched for return.");
    }
  }

  @Override
  public void enterAddSubExp(SysYParser.AddSubExpContext ctx) {
    //TODO: Error 6
    for (int i = 0; ctx.exp(i) != null; i++) {
      if(currentScope.resolve(ctx.exp(i).getText()) instanceof FunctionSymbol || currentScope.resolve(ctx.exp(i).getText()) instanceof ArraySymbol){
        //Error 6
        Main.ErrorExist = true;
        System.err.println("Error type 6 at Line " + ctx.PLUS().getSymbol().getLine() + ": type.Type mismatched for operands.");
        return;
      }
    }
  }

  @Override
  public void enterMulDivModExp(SysYParser.MulDivModExpContext ctx) {
    //TODO: Error 6
    for (int i = 0; ctx.exp(i) != null; i++) {
      if(currentScope.resolve(ctx.exp(i).getText()) instanceof FunctionSymbol || currentScope.resolve(ctx.exp(i).getText()) instanceof ArraySymbol){
        //Error 6
        Main.ErrorExist = true;
        System.err.println("Error type 6 at Line " + ctx.MUL().getSymbol().getLine() + ": type.Type mismatched for operands.");
        return;
      }
    }
  }

  @Override
  public void enterUnaryOpExp(SysYParser.UnaryOpExpContext ctx) {
    //TODO: Error 6
    if(currentScope.resolve(ctx.exp().getText()) instanceof FunctionSymbol || currentScope.resolve(ctx.exp().getText()) instanceof ArraySymbol){
      //Error 6
      Main.ErrorExist = true;
      System.err.println("Error type 6 at Line " + ctx.unaryOp().PLUS().getSymbol().getLine() + ": type.Type mismatched for operands.");
      return;
    }
  }

  @Override
  public void enterParens(SysYParser.ParensContext ctx) {
    //TODO
    if(currentScope.resolve(ctx.exp().getText()) instanceof FunctionSymbol || currentScope.resolve(ctx.exp().getText()) instanceof ArraySymbol){
      //Error 6
      Main.ErrorExist = true;
      System.err.println("Error type 6 at Line " + ctx.L_PAREN().getSymbol().getLine() + ": type.Type mismatched for operands.");
      return;
    }
  }
}
