// package se;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseScope implements Scope {
    private final Scope enclosingScope;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final MultiMap<String, String> symbolPositions = new MultiMap<>();
    private String name;

    public BaseScope(String name, Scope enclosingScope) {
        this.name = name;
        this.enclosingScope = enclosingScope;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Scope getEnclosingScope() {
        return this.enclosingScope;
    }

    @Override
    public Map<String, Symbol> getSymbols() {
        return this.symbols;
    }

    @Override
    public MultiMap<String, String> getSymbolPositions() {
        return symbolPositions;
    }

    @Override
    public void define(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
//        System.out.println("+ " + symbol.getName());
    }

    @Override
    public Symbol resolve(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
//            System.out.println("* " + name);
            return symbol;
        }

        if (enclosingScope != null) {
            return enclosingScope.resolve(name);
        }

//        System.err.println("Cannot find " + name);
        return null;
    }

    public void put(String key, String value, int flag)
    {
        if (symbolPositions.get(key) == null && flag == 0)
        {
//            symbolPositions.put(key, String.valueOf(new ArrayList<String>()));
            symbolPositions.put(key,value);
        } else if(symbolPositions.get(key) == null && flag == 1){
            enclosingScope.put(key,value,flag);
        } else {
            symbolPositions.put(key,value);
        }
    }

    @Override
    public String toString() {
        return "name "+ name + " symbols " + symbols.values();
    }
}
