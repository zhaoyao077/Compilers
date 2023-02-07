# lab6：Global And If

## 实验内容

- 全局变量的IR生成，包括全局变量的定义、赋值，使用和全局数组的定义、初始化、索引。
- If条件语句的翻译，需要注意IF、IF_ELSE语句是可以嵌套的。



## 代码设计

- 全局数组的翻译，全局数组与局部数组翻译的区别在于声明，全局数组使用```LLVMAddGlobal()```来声明，而不是```GEP```，并且全局数组的初始化需要用到```LLVMSetInitializer```，借助一个```ConstArray```来初始化数组。

  <img src="C:\Users\ARmi\AppData\Roaming\Typora\typora-user-images\image-20230113163753266.png" alt="image-20230113163753266" style="zoom:80%;" />

- IF条件语句的翻译方法比较固定，在获得了```cond```的值以后，判断该值是否为0来决定程序如何跳转。

  ![image-20230113164031255](C:\Users\ARmi\AppData\Roaming\Typora\typora-user-images\image-20230113164031255.png)



## 遇到的困难

- 在if语句的翻译时有些疑惑如何生成各分支的IR，在github上查看已有的代码解决了该问题。