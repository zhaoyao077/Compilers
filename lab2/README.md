# Compilers-lab2 note

## 实现功能

- 本次实验实现了对```SysY```语言的语法分析和高亮，将语法元素和词法元素按照深度和先后顺序输出为一个语法树结构（如下图）。

<img src="C:\Users\ARmi\AppData\Roaming\Typora\typora-user-images\image-20221129012740948.png" alt="image-20221129012740948" style="zoom:50%;" />

## 代码设计

- 在SysYParser.g4文件中，设计的特点是利用ANTLR的左递归语法特点，将```exp```和```cond```语法元素设计为左递归的形式。

- 在```Visitor```类中，新增了```displayLexer()```和```displayParser()```两个方法，分布用来输出语法单元和词法单元，因为两个单元的大小写和高亮不同，所以不能合并为一个方法。

  <img src="C:\Users\ARmi\AppData\Roaming\Typora\typora-user-images\image-20221129013254898.png" alt="image-20221129013254898" style="zoom:50%;" />

<img src="C:\Users\ARmi\AppData\Roaming\Typora\typora-user-images\image-20221129013525010.png" alt="image-20221129013525010" style="zoom:50%;" />



## 遇到的问题

1. 设计g4文件时，不太理解左递归的含义，在查阅ANTLR手册后解决该问题。

2. 在输出词法单元时，未找到获得```depth```属性的方法，经过类型强制转换后成功（如下图）。

   ![image-20221129013758282](C:\Users\ARmi\AppData\Roaming\Typora\typora-user-images\image-20221129013758282.png)