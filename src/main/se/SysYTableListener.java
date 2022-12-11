package se;

import se.sysy.SysYParserBaseListener;
import se.sysy.SysYParser;

public class SysYTableListener extends SysYParserBaseListener {

  private GlobalScope globalScope = null;
  private Scope currentScope = null;
  private int localScopeCounter = 0;

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
    FunctionSymbol fun = new FunctionSymbol(funName, currentScope);

    currentScope.define(fun);
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
    currentScope = currentScope.getEnclosingScope();
  }

  @Override
  public void exitBlock(SysYParser.BlockContext ctx) {
    currentScope = currentScope.getEnclosingScope();
  }

  /**
   * (3) When to define symbols?
   */
  @Override
  public void exitVarDecl(SysYParser.VarDeclContext ctx) {
    String typeName = ctx.bType().getText();
    Type type = (Type) globalScope.resolve(typeName);

    String varName = ctx.getText();
    VariableSymbol varSymbol = new VariableSymbol(varName, type);

    currentScope.define(varSymbol);
  }

  @Override
  public void exitFuncFParam(SysYParser.FuncFParamContext ctx) {
    String typeName = ctx.bType().getText();
    Type type = (Type) globalScope.resolve(typeName);

    String varName = ctx.getText();
    VariableSymbol varSymbol = new VariableSymbol(varName, type);

    currentScope.define(varSymbol);
  }

  /**
   * (4) When to resolve symbols?
   */
  @Override
  public void exitId(SysYParser.IdContext ctx) {
    String varName = ctx.IDENT().getText();
    currentScope.resolve(varName);
  }

}