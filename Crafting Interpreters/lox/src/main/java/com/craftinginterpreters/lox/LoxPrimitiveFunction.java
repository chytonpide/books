package com.craftinginterpreters.lox;

// 추가: primitive fun
abstract class LoxPrimitiveFunction implements LoxCallable {

  private final String name;

  LoxPrimitiveFunction(String name) {
    this.name = name;
  }


  @Override
  public String toString() {
    return "<native fn " + name + ">";
  }

  @Override
  public int arity() {
    return 0;
  }
}
