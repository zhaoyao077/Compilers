# Lab7: While

## 实验内容

- 翻译while循环
- 翻译break和continue语句



## 代码设计

- 对于不包含break和continue的while循环，只需要在每次```whileBody```的末尾跳转到```whileCondition```再次判断条件是否成立即可。

  核心代码如下：

  ![image-20230119180101233](C:\Users\ARmi\AppData\Roaming\Typora\typora-user-images\image-20230119180101233.png)

- 对于break和continue语句的处理，需要将每层```whileScope```循环压入栈内。然后遇到```break```时直接跳转到当前循环的```whileEnd```标签处，遇到```continue```时跳转到当前循环的```whileCondition```处直接进入下一次循环。

  新建的```WhileScope```类设计如下：

  ![image-20230119180649294](C:\Users\ARmi\AppData\Roaming\Typora\typora-user-images\image-20230119180649294.png)

