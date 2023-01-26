// package se;// lab3 code

import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
// import sysy.SysYLexer;
// import sysy.SysYParser;


public class Main{
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

    ParseTree tree = sysYParser.program();
    IRVisitor irVisitor = new IRVisitor();
    irVisitor.visit(tree);
    irVisitor.writeFile(args[1]);
  }
}
