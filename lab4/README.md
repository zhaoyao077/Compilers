# Compilers-lab4 note

## 实验内容

本次实验实现了对仅含一条return语句的main函数的IR翻译，重点在于通过访问语法树节点求得return的```exp```常量表达式的值。



## 代码设计

在```IRVisitor```，通过重写访问exp各个分支的方法，在访问分支时获得操作数，计算该节点的val值，并加入到```LLVM builder```中，以如下代码为例。

```java
@Override
    public LLVMValueRef visitPlusExp(SysYParser.PlusExpContext ctx) {
        LLVMValueRef op1 = visit(ctx.exp(0));
        LLVMValueRef op2 = visit(ctx.exp(1));
        
        if (ctx.PLUS() != null) {
            return LLVMBuildAdd(builder, op1, op2, "temp");
        } else if (ctx.MINUS() != null) {
            return LLVMBuildSub(builder, op1, op2, "temp");
        }
        return null;
    }
```



## 遇到的困难

- 在配置LLVM环境的过程中，下载jar包，maven换源、api使用都遇到了不同程度的困难，通过查看手册和搜索网络获得了解决办法。
- 在实验过程中，起初没有理解实验要求，直接对于return的exp进行解析，自己写了个递归算法...导致debug十分痛苦，在理解了题目要求后重新开始，最终完成实验。