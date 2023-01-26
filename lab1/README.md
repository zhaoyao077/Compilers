# Compilers-lab1

## 实现功能

- 本次lab实现了对于SysY语言的词法分析，使用ANTLR4工具自动化生成了词法分析器SysYLexer.java，随后在Main函数中对于词法分析的结果进行分类处理。

## 代码设计

- 如果源文件有词法错误，就只输出错误信息。通过是否调用了```MyErrorListener```类的```syntaxError()```方法来判断。其中```output```是```MyErrorListener```类的一个public属性，如果调用了```syntaxError()```输出错误词法信息，就会在方法中**设该属性为true**，那么就不输出tokens。

  ```java
  // in MyErrorListener.syntaxError()
  System.err.println("Error type A at Line " + line + ": msg");
  output = true;
  ```

- 这里使用了jdk8的特性**lambda表达式**，这样处理List类型的tokens变量很方便，也具有很强的可读性。

  ```java
  // in Main.main()
  if(! myErrorListener.output)
        tokens.forEach(t -> disPlay(t,sysYLexer));
  ```

- 在display方法中，对于**16进制和8进制**进行特殊处理。

  ```java
  // Main.display
  public static void disPlay(CommonToken t, Lexer lexer){
      String name = lexer.getVocabulary().getSymbolicName(t.getType());
      // 删去一组单引号
      if(name.charAt(0) == '\'')
        name = name.substring(1,name.length()-1);
  	// 处理16进制和8进制，直接使用parseInt()
      String text = t.getText();
      if (text.startsWith("0x") || text.startsWith("0X")){
        text = "" + Integer.parseInt(text.substring(2),16);
      } else if (text.startsWith("0") && text.length() > 1){
        text = "" + Integer.parseInt(text.substring(1),8);
      }
  	
      int lineNo = t.getLine();
      // 格式化输出
      System.err.println(name + " " + text + " at Line " + lineNo + ".");
    }
  ```

## 遇到的问题

1. 在初期环境配置中，不了解Makefile文件的语法含义，导致困难重重，最终通过阅读实验手册和Makefile教程解决。
2. 在Linux虚拟机环境中编写代码困难的问题，通过共享文件夹+IDEA解决。
3. 不清楚CommonToken的API，通过查阅javadoc了解其用法。