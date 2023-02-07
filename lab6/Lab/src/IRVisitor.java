//lab6 code
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

// import sysy.SysYParser;
// import sysy.SysYParserBaseVisitor;

import java.util.HashMap;
import java.util.Map;

public class IRVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    private GlobalScope globalScope = null;
    private Scope currentScope = null;
    private int localScopeCounter = 0;
    private Map<String,String> functionRetTypeMap = new HashMap<String, String>();
    private Map<String,Integer> globalVarValueMap = new HashMap<String,Integer>();
    private Map<String,LLVMValueRef[]> globalArrayMap = new HashMap<>();

    //LLVM global variables
    public static final BytePointer error = new BytePointer();
    private final LLVMModuleRef module = LLVMModuleCreateWithName("module");
    private final LLVMBuilderRef builder = LLVMCreateBuilder();
    private final LLVMTypeRef i32Type = LLVMInt32Type();
    private final LLVMTypeRef voidType = LLVMVoidType();
    private final LLVMValueRef zero = LLVMConstInt(i32Type, 0, 0);
    private final LLVMValueRef one = LLVMConstInt(i32Type, 1, 0);
    LLVMValueRef curBlock;

    public IRVisitor() {
        //初始化LLVM
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
    }

    public void writeFile(String filePath) {
        if (LLVMPrintModuleToFile(module, filePath, error) != 0) {
            LLVMDisposeMessage(error);
        }
    }

    @Override
    public LLVMValueRef visitProgram(SysYParser.ProgramContext ctx) {
        //进入
        globalScope = new GlobalScope(null);
        currentScope = globalScope;
        LLVMValueRef ret = super.visitProgram(ctx);
        //退出
        currentScope = currentScope.getEnclosingScope();
        return ret;
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        //返回值
        LLVMTypeRef returnType = ctx.funcType().getText().equals("int") ? i32Type : voidType;

        //形参
        int argumentCount = 0;
        if(ctx.funcFParams() != null)
            argumentCount = ctx.funcFParams().funcFParam().size();
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(argumentCount);
        for (int i = 0; i < argumentCount; i++) {
            argumentTypes.put(i,i32Type);
        }

        //函数类型(包括返回值和形参列表)
        LLVMTypeRef functionType = LLVMFunctionType(returnType, argumentTypes, argumentCount, 0);

        //函数
        LLVMValueRef function = LLVMAddFunction(module, ctx.IDENT().getText(), functionType);

        // 进入新的 Scope，定义新的 Symbol
        currentScope.define(ctx.IDENT().getText(),function);
        currentScope = new FunctionScope(ctx.IDENT().getText(),currentScope);
        functionRetTypeMap.put(ctx.IDENT().getText(),ctx.funcType().getText());

        //添加block
        curBlock = function;
        LLVMBasicBlockRef block = LLVMAppendBasicBlock(function, ctx.IDENT().getText()+"Entry");
        LLVMPositionBuilderAtEnd(builder, block);

        //初始化参数列表
        for (int i = 0; i < argumentCount; i++) {
            SysYParser.FuncFParamContext FParam = ctx.funcFParams().funcFParam(i);
            LLVMTypeRef paramType = i32Type;
            String varName = FParam.IDENT().getText();
            LLVMValueRef varPointer = LLVMBuildAlloca(builder, paramType, varName);
            currentScope.define(varName, varPointer);
            LLVMValueRef argValue = LLVMGetParam(function, i);
            LLVMBuildStore(builder, argValue, varPointer);
        }

        LLVMValueRef ret = super.visitFuncDef(ctx);
        if(ctx.funcType().getText().equals("void")){
            LLVMBuildRetVoid(builder);
        }
        currentScope = currentScope.getEnclosingScope();
        return ret;
    }

    @Override
    public LLVMValueRef visitCallFuncExp(SysYParser.CallFuncExpContext ctx) {
        String functionName = ctx.IDENT().getText();
        LLVMValueRef function = currentScope.resolve(functionName);
        int argsCount = 0;
        if(ctx.funcRParams() != null){
            argsCount = ctx.funcRParams().param().size();
        }
        PointerPointer<LLVMValueRef> args = new PointerPointer<>(argsCount);
        for (int i = 0; i < argsCount; i++) {
            args.put(i,visit(ctx.funcRParams().param(i).exp()));
        }
        return LLVMBuildCall(builder, function, args, argsCount, "");
    }

    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        // 进入新的 Scope
        LocalScope localScope = new LocalScope(currentScope);
        String localScopeName = localScope.getName() + localScopeCounter;
        localScope.setName(localScopeName);
        localScopeCounter++;
        currentScope = localScope;

        LLVMValueRef ret = super.visitBlock(ctx);
        // 回到上一层 Scope
        currentScope = currentScope.getEnclosingScope();
        return ret;
    }

    @Override
    public LLVMValueRef visitVarDecl(SysYParser.VarDeclContext ctx) {
        if(currentScope == globalScope){//global var
            for (SysYParser.VarDefContext varDefContext : ctx.varDef()){
                String varName = varDefContext.IDENT().getText();
                int dim = 0;
                LLVMValueRef globalVar;

                if(varDefContext.L_BRACKT(0) == null) {//int a = 1;
                    LLVMValueRef initVal = zero;
                    if(varDefContext.ASSIGN() != null) {
                        initVal = visit(varDefContext.initVal());
                    }
                    globalVar = LLVMAddGlobal(module, i32Type, varName);
                    LLVMSetInitializer(globalVar, initVal);
                    globalVarValueMap.put(varName,(int)LLVMConstIntGetSExtValue(initVal));
                } else {//global array
                    dim = 1;
                    int initValCount = 0;
                    if(varDefContext.initVal() != null)
                        initValCount = varDefContext.initVal().initVal().size();
                    //TODO
                    int elemCount = 0;
                    if(globalVarValueMap.get(varDefContext.constExp(0).getText()) != null){
                        elemCount = globalVarValueMap.get(varDefContext.constExp(0).getText());
                    } else {
                        elemCount = (int) LLVMConstIntGetSExtValue(visit(varDefContext.constExp(0)));
                    }
                    LLVMTypeRef arrayType = LLVMArrayType(i32Type,elemCount);
                    globalVar = LLVMAddGlobal(module,arrayType,varName);
                    LLVMValueRef []initArray = new LLVMValueRef[elemCount];
                    for (int i = 0; i < elemCount; i++) {
                        if(i < initValCount){
                            initArray[i] = visit(varDefContext.initVal().initVal(i));
                        } else {
                            initArray[i] = zero;
                        }
                    }
                    LLVMSetInitializer(globalVar,LLVMConstArray(i32Type, new PointerPointer(initArray), elemCount));
                    globalArrayMap.put(varName,initArray);
                }

                currentScope.define(varName,globalVar);
                currentScope.addDimMap(varName,dim);
            }
        } else {//local var
            for (SysYParser.VarDefContext varDefContext : ctx.varDef()) {
                String varName = varDefContext.IDENT().getText();
                LLVMValueRef varPointer;
                int dim = 0;

                if (varDefContext.ASSIGN() != null) {
                    SysYParser.ExpContext expContext = varDefContext.initVal().exp();
                    if (expContext != null) {
                        varPointer = LLVMBuildAlloca(builder, i32Type, varName);
                        LLVMValueRef initVal = visit(expContext);
                        LLVMBuildStore(builder, initVal, varPointer);
                    } else {
                        dim = 1;
                        int initValCount = varDefContext.initVal().initVal().size();
                        int elementCount = (int) LLVMConstIntGetSExtValue(visit(varDefContext.constExp(0)));
                        varPointer = LLVMBuildAlloca(builder, LLVMArrayType(i32Type, elementCount), varName);
                        LLVMValueRef[] initArray = new LLVMValueRef[elementCount];
                        for (int i = 0; i < elementCount; i++) {
                            if (i < initValCount) {
                                initArray[i] = visit(varDefContext.initVal().initVal(i));
                            } else {
                                initArray[i] = zero;
                            }
                        }
                        // fill in initArray
                        buildGEP(elementCount, varPointer, initArray);
                    }
                } else {
                    varPointer = LLVMBuildAlloca(builder, i32Type, varName);
                }

                currentScope.define(varName, varPointer);
                currentScope.addDimMap(varName, dim);
            }
        }
        return null;
    }

    private int resloveConstExp(SysYParser.ConstExpContext ctx){
        if(currentScope.resolve(ctx.getText()) == null){//index = 3
            return (int)LLVMConstIntGetSExtValue(visit(ctx.exp()));
        } else {//index = b
            return globalVarValueMap.get(ctx.exp().getText());
        }
    }

    @Override
    public LLVMValueRef visitConstDecl(SysYParser.ConstDeclContext ctx) {
        if(currentScope == globalScope){//global var
            for (SysYParser.ConstDefContext varDefContext : ctx.constDef()){
                String varName = varDefContext.IDENT().getText();
                int dim = 0;
                LLVMValueRef globalVar;

                if(varDefContext.L_BRACKT(0) == null) {//int a = 1;
                    LLVMValueRef initVal = zero;
                    if(varDefContext.ASSIGN() != null) {
                        initVal = visit(varDefContext.constInitVal());
                    }
                    globalVar = LLVMAddGlobal(module, i32Type, varName);
                    LLVMSetInitializer(globalVar, initVal);
                    globalVarValueMap.put(varName,(int)LLVMConstIntGetSExtValue(initVal));
                } else {//int a[b] = { 0,1,1 };
                    dim = 1;
                    int initValCount = 0;
                    if(varDefContext.constInitVal() != null)
                        initValCount = varDefContext.constInitVal().constInitVal().size();
                    //TODO:elemCount
                    int elemCount = resloveConstExp(varDefContext.constExp(0));
                    LLVMTypeRef arrayType = LLVMArrayType(i32Type,elemCount);
                    globalVar = LLVMAddGlobal(module,arrayType,varName);
                    LLVMValueRef []initArray = new LLVMValueRef[elemCount];
                    for (int i = 0; i < elemCount; i++) {
                        if(i < initValCount){
                            initArray[i] = visit(varDefContext.constInitVal().constInitVal(i));
                        } else {
                            initArray[i] = zero;
                        }
                    }
                    LLVMSetInitializer(globalVar,LLVMConstArray(i32Type, new PointerPointer(initArray), elemCount));
                    globalArrayMap.put(varName,initArray);
                }

                currentScope.define(varName,globalVar);
                currentScope.addDimMap(varName,dim);
            }
        } else {
            for (SysYParser.ConstDefContext varDefContext : ctx.constDef()) {
                String varName = varDefContext.IDENT().getText();
                LLVMValueRef varPointer;
                int dim = 0;

                if (varDefContext.ASSIGN() != null) {
                    SysYParser.ConstExpContext expContext = varDefContext.constInitVal().constExp();
                    if (expContext != null) {
                        varPointer = LLVMBuildAlloca(builder, i32Type, varName);
                        LLVMValueRef initVal = visit(expContext);
                        LLVMBuildStore(builder, initVal, varPointer);
                    } else {
                        dim = 1;
                        int initValCount = 0;
                        if(varDefContext.constInitVal() != null)
                            initValCount = varDefContext.constInitVal().constInitVal().size();
                        int elementCount = (int) LLVMConstIntGetSExtValue(visit(varDefContext.constExp(0)));
                        varPointer = LLVMBuildAlloca(builder, LLVMArrayType(i32Type, elementCount), varName);
                        LLVMValueRef[] initArray = new LLVMValueRef[elementCount];
                        for (int i = 0; i < elementCount; i++) {
                            if (i < initValCount) {
                                initArray[i] = visit(varDefContext.constInitVal().constInitVal(i));
                            } else {
                                initArray[i] = zero;
                            }
                        }
                        // fill in initArray
                        buildGEP(elementCount, varPointer, initArray);
                    }
                } else {
                    varPointer = LLVMBuildAlloca(builder, i32Type, varName);
                }

                currentScope.define(varName, varPointer);
                currentScope.addDimMap(varName, dim);
            }
        }
        return null;
    }

    private void buildGEP(int elementCount, LLVMValueRef varPointer, LLVMValueRef[] initArray) {
        LLVMValueRef[] arrayPointer = new LLVMValueRef[2];
        arrayPointer[0] = zero;
        for (int i = 0; i < elementCount; i++) {
            arrayPointer[1] = LLVMConstInt(i32Type, i, 0);
            PointerPointer<LLVMValueRef> indexPointer = new PointerPointer<>(arrayPointer);
            LLVMValueRef elementPtr = LLVMBuildGEP(builder, varPointer, indexPointer, 2, "pointer");
            LLVMBuildStore(builder, initArray[i], elementPtr);
        }
    }

    private LLVMValueRef loadGEP(String arrayName, LLVMValueRef varPointer,LLVMValueRef index){
        LLVMValueRef[] arrayPointer = new LLVMValueRef[2];
        arrayPointer[0] = zero;
        arrayPointer[1] = index;
        PointerPointer<LLVMValueRef> indexPointer = new PointerPointer<>(arrayPointer);
        LLVMValueRef elementPtr = LLVMBuildGEP(builder, varPointer, indexPointer, 2, "temp");
        return LLVMBuildLoad(builder,elementPtr,arrayName + index);
    }

    private LLVMValueRef storeGEP(LLVMValueRef rightVal,LLVMValueRef varPointer,int index){
        LLVMValueRef[] arrayPointer = new LLVMValueRef[2];
        arrayPointer[0] = zero;
        arrayPointer[1] = LLVMConstInt(i32Type, index, 0);
        PointerPointer<LLVMValueRef> indexPointer = new PointerPointer<>(arrayPointer);
        LLVMValueRef elementPtr = LLVMBuildGEP(builder, varPointer, indexPointer, 2, "pointer");
        return LLVMBuildStore(builder, rightVal, elementPtr);
    }

    @Override
    public LLVMValueRef visitLvalExp(SysYParser.LvalExpContext ctx) {
        return visitLVal(ctx.lVal());
    }

    @Override
    public LLVMValueRef visitLVal(SysYParser.LValContext ctx) {
        String varName = ctx.IDENT().getText();
        LLVMValueRef valueRef = currentScope.resolve(varName);
        System.out.println("dim: " + currentScope.getDim(varName));
        if(currentScope == globalScope){
            if(currentScope.getDim(varName) == 0 && valueRef != null)
                return LLVMBuildLoad(builder,valueRef,varName);
        }
        if(currentScope.getDim(varName) == 0){
            return LLVMBuildLoad(builder, valueRef, varName);
        } else if(ctx.exp(0) != null){
            return loadGEP(varName,valueRef,visit(ctx.exp(0)));
        }
        return LLVMBuildLoad(builder, valueRef, ctx.getText());
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
    public LLVMValueRef visitReturnStmt(SysYParser.ReturnStmtContext ctx) {
        Scope temp = currentScope;
        while(!(temp instanceof FunctionScope)){
            temp = temp.getEnclosingScope();
        }
        if(functionRetTypeMap.get(temp.getName()) != null){
            if(functionRetTypeMap.get(temp.getName()).equals("int")) {
                LLVMBuildRet(builder, visit(ctx.exp()));
            } else {
                LLVMBuildRetVoid(builder);
            }
        }

        return null;
    }

    @Override
    public LLVMValueRef visitAssignStmt(SysYParser.AssignStmtContext ctx) {
        //TODO
        String leftVarName = ctx.lVal().IDENT().getText();
        LLVMValueRef leftValRef = currentScope.resolve(leftVarName);
        LLVMValueRef right = visit(ctx.exp());
        if(currentScope.getDim(leftVarName) == 0){// a = 0;
            return LLVMBuildStore(builder,right,leftValRef);
        } else if(ctx.lVal().exp(0) != null){//   arr[1] = a;
            int index = (int) LLVMConstIntGetSExtValue(visit(ctx.lVal().exp(0)));
            return storeGEP(right,leftValRef,index);
        } else {//arr1 = arr2;
            return LLVMBuildStore(builder,right,leftValRef);
        }
    }

    @Override
    public LLVMValueRef visitConditionStmt(SysYParser.ConditionStmtContext ctx) {
        LLVMValueRef cond = LLVMBuildICmp(builder,LLVMIntNE,zero,LLVMBuildZExt(builder, visit(ctx.cond()), i32Type, "temp"),"temp");

        LLVMBasicBlockRef true_entry = LLVMAppendBasicBlock(curBlock,"true");
        LLVMBasicBlockRef false_entry = LLVMAppendBasicBlock(curBlock,"false");
        LLVMBasicBlockRef then = LLVMAppendBasicBlock(curBlock,"then");

        LLVMBuildCondBr(builder,cond,true_entry,false_entry);

        //Append code after true:
        LLVMPositionBuilderAtEnd(builder, true_entry);
        visit(ctx.stmt(0));
        LLVMBuildBr(builder,then);

        //Append code after false:
        LLVMPositionBuilderAtEnd(builder, false_entry);
        if(ctx.stmt(1) == null){//no else{...}
            LLVMBuildBr(builder,then);
        } else {
            visit(ctx.stmt(1));
            LLVMBuildBr(builder,then);
        }

        //Append code after then:
        LLVMPositionBuilderAtEnd(builder, then);

        return null;
    }

    @Override
    public LLVMValueRef visitExpCond(SysYParser.ExpCondContext ctx) {
        return visit(ctx.exp());
    }

    @Override
    public LLVMValueRef visitLtCond(SysYParser.LtCondContext ctx) {
        int op = 0;
        if(ctx.GT() != null){
            op = LLVMIntSGT;
        } else if(ctx.GE() != null){
            op = LLVMIntSGE;
        } else if(ctx.LT() != null){
            op = LLVMIntSLT;
        } else if(ctx.LE() != null){
            op = LLVMIntSLE;
        }
        LLVMValueRef condition = LLVMBuildICmp(builder,op,visit(ctx.cond(0)),visit(ctx.cond(1)),"temp");
        LLVMValueRef zext = LLVMBuildZExt(builder, condition, i32Type, "temp");
        return zext;
    }

    @Override
    public LLVMValueRef visitEqCond(SysYParser.EqCondContext ctx) {
        int op = 0;
        if(ctx.EQ() != null){
            op = LLVMIntEQ;
        } else if(ctx.NEQ() != null){
            op = LLVMIntNE;
        }
        LLVMValueRef condition = LLVMBuildICmp(builder,op,visit(ctx.cond(0)),visit(ctx.cond(1)),"temp");
        LLVMValueRef zext = LLVMBuildZExt(builder, condition, i32Type, "temp");
        return zext;
    }
}
