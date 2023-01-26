// package se;

import java.util.Map;

public interface Scope {
    public String getName();

    public void setName(String name);

    public Scope getEnclosingScope();

    public Map<String, Symbol> getSymbols();

    public void define(Symbol symbol);

    public MultiMap<String, String> getSymbolPositions();

    public void put(String key, String value, int flag);

    public Symbol resolve(String name);
}
