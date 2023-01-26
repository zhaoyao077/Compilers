// package se;

public class BaseSymbol implements Symbol {
    String name;
    Type type;
    // the dimension of array, the value is default 0 if the symbol is not array
    int dim = 0;


    public BaseSymbol(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public BaseSymbol(String name, Type type, int dim) {
        this.name = name;
        this.type = type;
        this.dim = dim;
    }

    public int getDim() {
        return dim;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String toString() {
        return "name"+ name + "type" + type;
    }
}
