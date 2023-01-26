// package se;

import java.util.ArrayList;

public class FunctionSymbol extends BaseScope implements Symbol {
   BasicType retType;
   ArrayList<Type> paramsType;

   public FunctionSymbol(String name, Scope enclosingScope, BasicType type) {
       super(name, enclosingScope);
       this.retType = type;
       paramsType = new ArrayList<>();
   }

   public BasicType getRetType() {
       return retType;
   }
}
