// package se;// lab3 code

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
// import se.sysy.SysYLexer;
// import se.sysy.SysYParser;


public class Main{
  public static String [] ruleNames;
  public static String [] lexerRuleNames;
  public static Scope targetScope = null;
  public static int lineNo;
  public static int column;
  public static String newName;
  public static String oldName;
  public static boolean ErrorExist = false;

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.println("input path is required");
      return;
    }
    String source = args[0];
    lineNo = Integer.parseInt(args[1]);
    column = Integer.parseInt(args[2]);
    newName = args[3];

    CharStream input = CharStreams.fromFileName(source);
    SysYLexer sysYLexer = new SysYLexer(input);

    CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
    SysYParser sysYParser = new SysYParser(tokens);
    sysYParser.removeErrorListeners();

    Main.ruleNames = sysYParser.getRuleNames();
    Main.lexerRuleNames = sysYLexer.getRuleNames();

    ParseTree tree = sysYParser.program();

    ParseTreeWalker walker = new ParseTreeWalker();
    SymbolTableListener symbolTableListener = new SymbolTableListener();

     walker.walk(symbolTableListener, tree);

     if(!ErrorExist) {
       MyVisitor visitor = new MyVisitor();
       visitor.visit(tree);
     }
  }

  public static boolean isTarget(int m, int n){

    AtomicBoolean val = new AtomicBoolean(false);
    targetScope.getSymbolPositions().get(oldName).forEach(item -> {
      if(item.equals(m + " " + n)){
        val.set(true);
      }
    });

    return val.get();
  }
}
