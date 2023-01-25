//lab2 code
import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.BitSet;


public class Main{
  public static String [] ruleNames;
  public static String [] lexerRuleNames;

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.println("input path is required");
      return;
    }
    String source = args[0];
    CharStream input = CharStreams.fromFileName(source);
    SysYLexer sysYLexer = new SysYLexer(input);

    CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
    SysYParser sysYParser = new SysYParser(tokens);

    sysYParser.removeErrorListeners();
    MyErrorListener myErrorListener = new MyErrorListener();
    sysYParser.addErrorListener(myErrorListener);


    ParseTree tree = sysYParser.program();
    Visitor visitor = new Visitor();

    Main.ruleNames = sysYParser.getRuleNames();

    Main.lexerRuleNames = sysYLexer.getRuleNames();

    if(!myErrorListener.output){
      visitor.visit(tree);//TODO
    }

  }// close main
}// close class

class Visitor extends SysYParserBaseVisitor<Void> {
  private int depth = 0;

  @Override
  public Void visitChildren(RuleNode node) {
    //TODO
    depth = node.getRuleContext().depth();
    displayParser(Main.ruleNames[node.getRuleContext().getRuleIndex()], depth);

    return super.visitChildren(node);
  }

  public void displayParser(String text, int depth){
    for (int i = 1; i < depth; i++) {
      System.err.print("  ");
    }
    System.err.println(text.substring(0,1).toUpperCase() + text.substring(1));
  }

  @Override
  public Void visitTerminal(TerminalNode node) {
    //TODO
    int type = node.getSymbol().getType();
    String word = "";
    if(type != -1){
      word = Main.lexerRuleNames[type-1];
    }

    RuleContext ruleContext = new RuleContext((RuleContext) node.getParent(), 0);
    depth = ruleContext.depth();
    displayLexer(node.getSymbol().getText(), word, depth);

    return super.visitTerminal(node);
  }

  public void displayLexer(String text, String word, int depth){
    String [] reservedWords = {"const", "int", "void", "if", "else", "while", "break", "continue", "return"};
    String [] operators = {"plus", "minus", "mul", "div", "mod", "assign", "eq", "neq", "lt", "gt", "le", "ge", "not", "and", "or"};
    String [] excludedWords = {"L_PAREN", "R_PAREN", "L_BRACE", "R_BRACE", "L_BRACKT", "R_BRACKT", "SEMICOLON", "COMMA"};
    if(text.equals("<EOF>")){
      return;
    }
    for(String excludedWord:excludedWords){
      if(word.equals(excludedWord))
        return;
    }
    String color = "";
    for (String reservedWord : reservedWords) {
      if (word.toLowerCase().equals(reservedWord)) {
        color = "[orange]";
        break;
      }
    }
    for (String operator : operators) {
      if (word.toLowerCase().equals(operator)) {
        color = "[blue]";
        break;
      }
    }
    if(word.equals("IDENT")){
      color = "[red]";
    }
    if(word.equals("INTEGR_CONST")){
      color = "[green]";
    }

    // 处理8进制,16进制
    if (text.startsWith("0x") || text.startsWith("0X")){
      text = "" + Integer.parseInt(text.substring(2),16);
    } else if (text.startsWith("0") && text.length() > 1){
      text = "" + Integer.parseInt(text.substring(1),8);
    }

    //output
    for (int i = 1; i < depth; i++) {
        System.err.print("  ");
    }
    System.err.println(text + " " + word + color);

  }

}


class MyErrorListener implements ANTLRErrorListener{
  public boolean output = false;
  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    System.err.println("Error type B at Line " + line + ": msg");
    output = true;
  }

  @Override
  public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

  }

  @Override
  public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

  }

  @Override
  public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

  }
}
