package se;


public class BaseSymbol implements Symbol {
  final String name;
  final Type type;

  public BaseSymbol(String name, Type type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public String toString() {
    return "name " + name + "type " + type;
  }
}