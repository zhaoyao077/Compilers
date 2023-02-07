//lab8 code
import org.bytedeco.llvm.LLVM.LLVMValueRef;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaseScope implements Scope{
    private final Scope enclosingScope;
    private final Map<String, LLVMValueRef> symbols = new LinkedHashMap<>();
    private String name;
    private final Map<String, Integer> dimMap = new HashMap<>();

    public BaseScope(String name, Scope enclosingScope){
        this.name = name;
        this.enclosingScope = enclosingScope;
    }

    @Override
    public void addDimMap(String name, int dim) {
        dimMap.put(name,dim);
    }

    @Override
    public int getDim(String name) {
        if(dimMap.get(name) != null)
            return dimMap.get(name);
        if(enclosingScope != null){
            return enclosingScope.getDim(name);
        }
        return -1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name){
        this.name = name;
    }

    @Override
    public Scope getEnclosingScope() {
        return enclosingScope;
    }

    @Override
    public Map<String, LLVMValueRef> getSymbols() {
        return symbols;
    }

    @Override
    public void define(String name, LLVMValueRef valueRef) {
        symbols.put(name,valueRef);
    }

    @Override
    public boolean isDefinedSymbol(String name) {
        return symbols.get(name) != null;
    }

    @Override
    public LLVMValueRef resolve(String name) {
        LLVMValueRef symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }

        if (enclosingScope != null) {
            return enclosingScope.resolve(name);
        }

        return null;
    }
}
