// package se;

public class BasicType extends BaseSymbol implements Type{
    public BasicType(String name) {
        super(name, null);
    }

    @Override
    public String toString() {
        return name;
    }
}
