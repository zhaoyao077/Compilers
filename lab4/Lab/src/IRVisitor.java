import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
// import sysy.SysYParser;
// import sysy.SysYParserBaseVisitor;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class IRVisitor extends SysYParserBaseVisitor<LLVMValueRef> {

    public static final BytePointer error = new BytePointer();
    LLVMModuleRef module;
    LLVMBuilderRef builder;
    LLVMTypeRef i32Type;
    LLVMValueRef result;

    public IRVisitor() {
        //初始化LLVM
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
        module = LLVMModuleCreateWithName("module");
        builder = LLVMCreateBuilder();
        i32Type = LLVMInt32Type();
    }

    public void writeFile(String filePath) {
        if (LLVMPrintModuleToFile(module, filePath, error) != 0) {
            LLVMDisposeMessage(error);
        }
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        if ("main".equals(ctx.IDENT().getText())) {
            PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);

            LLVMTypeRef ft = LLVMFunctionType(i32Type, argumentTypes, 0, 0);

            LLVMValueRef function = LLVMAddFunction(module, "main", ft);

            LLVMBasicBlockRef block = LLVMAppendBasicBlock(function, "mainEntry");

            LLVMPositionBuilderAtEnd(builder, block);
        }
        return super.visitFuncDef(ctx);
    }

    @Override
    public LLVMValueRef visitUnaryOpExp(SysYParser.UnaryOpExpContext ctx) {
        SysYParser.UnaryOpContext op = ctx.unaryOp();

        LLVMValueRef val = visit(ctx.exp());
        LLVMValueRef zero = LLVMConstInt(i32Type, 0, 0);

        if (op.PLUS() != null) {
            return val;
        } else if (op.MINUS() != null) {
            return LLVMBuildSub(builder, zero, val, "temp");
        } else if (op.NOT() != null) {
            LLVMValueRef temp = LLVMBuildICmp(builder, LLVMIntNE, zero, val, "temp");
            LLVMValueRef temp1 = LLVMBuildXor(builder, temp, LLVMConstInt(LLVMInt1Type(), 1, 0), "temp1");
            return LLVMBuildZExt(builder, temp1, i32Type, "temp");
        }

        return null;
    }

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

    @Override
    public LLVMValueRef visitMulExp(SysYParser.MulExpContext ctx) {
        LLVMValueRef op1 = visit(ctx.exp(0));
        LLVMValueRef op2 = visit(ctx.exp(1));

        if (ctx.MOD() != null) {
            return LLVMBuildSRem(builder, op1, op2, "temp");
        } else if (ctx.MUL() != null) {
            return LLVMBuildMul(builder, op1, op2, "temp");
        } else if (ctx.DIV() != null) {
            return LLVMBuildSDiv(builder, op1, op2, "temp");
        }

        return null;
    }

    @Override
    public LLVMValueRef visitNumberExp(SysYParser.NumberExpContext ctx) {
        String num = ctx.number().getText();
        int val;

        //处理十六进制和八进制
        if (num.startsWith("0x") || num.startsWith("0X")) {
            val = Integer.parseInt(num.substring(2), 16);
        } else if (num.startsWith("0") && num.length() > 1) {
            val = Integer.parseInt(num, 8);
        } else {
            val = Integer.parseInt(num);
        }

        return LLVMConstInt(i32Type, val, 0);
    }

    @Override
    public LLVMValueRef visitExpParenthesis(SysYParser.ExpParenthesisContext ctx) {
        return visit(ctx.exp());
    }

    @Override
    public LLVMValueRef visitStmt(SysYParser.StmtContext ctx) {
        if (ctx.exp() != null) {
            result = visit(ctx.exp());
        }
        if (ctx.RETURN() != null) {
            LLVMBuildRet(builder, result);
        }

        return null;
    }


}
