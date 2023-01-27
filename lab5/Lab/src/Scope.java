//lab5 code
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.Map;

public interface Scope {
    String getName();

    void setName(String name);

    Scope getEnclosingScope();

    Map<String, LLVMValueRef> getSymbols();

    void define(String name, LLVMValueRef valueRef);

    boolean isDefinedSymbol(String name);

    LLVMValueRef resolve(String name);

    void addDimMap(String name, int dim);

    int getDim(String name);
}
