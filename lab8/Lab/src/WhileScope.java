import org.bytedeco.llvm.LLVM.LLVMBasicBlockRef;
import java.util.HashMap;

public class WhileScope extends BaseScope{
    private HashMap<String, LLVMBasicBlockRef> block = new HashMap<>();

    public WhileScope(String name, Scope enclosingScope) {
        super(name, enclosingScope);
    }

    public void setBlock(String key, LLVMBasicBlockRef block) {
        this.block.put(key, block);
    }

    public LLVMBasicBlockRef getBlock(String key) {
        return this.block.get(key);
    }
}
