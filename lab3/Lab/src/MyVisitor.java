// package se;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
// import se.sysy.SysYParserBaseVisitor;

class MyVisitor extends SysYParserBaseVisitor<Void> {
    private int depth = 0;

    @Override
    public Void visitChildren(RuleNode node) {
        depth = node.getRuleContext().depth();
        displayParser(Main.ruleNames[node.getRuleContext().getRuleIndex()], depth);

        return super.visitChildren(node);
    }

    public void displayParser(String text, int depth){
      if(text.equals("funBlock")){
            text = "block";
        }
        for (int i = 1; i < depth; i++) {
            System.err.print("  ");
        }
        System.err.println(text.substring(0,1).toUpperCase() + text.substring(1));
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        int type = node.getSymbol().getType();
        String word = "";
        if(type != -1){
            word = Main.lexerRuleNames[type-1];
        }

        RuleContext ruleContext = new RuleContext((RuleContext) node.getParent(), 0);
        depth = ruleContext.depth();

        String text = node.getSymbol().getText();
        if(Main.isTarget(node.getSymbol().getLine(), node.getSymbol().getCharPositionInLine())){
            text = Main.newName;
        }

        displayLexer(text, word, depth);

        return super.visitTerminal(node);
    }

    public void displayLexer(String text, String word, int depth){
        //excluded
        String [] excludedWords = {"L_PAREN", "R_PAREN", "L_BRACE", "R_BRACE", "L_BRACKT", "R_BRACKT", "SEMICOLON", "COMMA"};
        if(text.equals("<EOF>")){
            return;
        }
        for(String excludedWord:excludedWords){
            if(word.equals(excludedWord)) {
                return;
            }
        }
        //color
        String [] reservedWords = {"const", "int", "void", "if", "else", "while", "break", "continue", "return"};
        String [] operators = {"plus", "minus", "mul", "div", "mod", "assign", "eq", "neq", "lt", "gt", "le", "ge", "not", "and", "or"};
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
        if(word.equals("IDENT")){ color = "[red]"; }
        if(word.equals("INTEGR_CONST")){ color = "[green]"; }

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
