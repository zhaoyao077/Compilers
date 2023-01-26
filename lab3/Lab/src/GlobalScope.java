// package se;

import java.util.List;

public class GlobalScope extends BaseScope {
    public GlobalScope(Scope enclosingScope) {
        super("GlobalScope", enclosingScope);
        define(new BasicType("int"));
        define(new BasicType("double"));
        define(new BasicType("void"));
    }
}
