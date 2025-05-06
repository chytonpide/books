package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

public class LoxArray {

  private final List<Object> items = new ArrayList<>();

  LoxArray() {
  }

  void add(Object item) {
    items.add(item);
  }

  Double getLength(Token name) {
    if (name.lexeme.equals("length")) {
      return (double) items.size();
    }

    throw new RuntimeError(name, "Not supported API.");
  }

  Object get(Token square, Object index) {
    if(!(index instanceof Double)) {
      throw new RuntimeError(square, "index must be a number.");
    }

    int i = ((Double) index).intValue();

    if(items.size() <= i) {
      throw new RuntimeError(square, "index too large.");
    }

    return items.get(i);
  }

  Object set(Token square, Object index, Object value) {
    if(!(index instanceof Double)) {
      throw new RuntimeError(square, "index must be a number.");
    }

    int i = ((Double) index).intValue();

    if(items.size() <= i) {
      throw new RuntimeError(square, "index too large.");
    }

    return items.set(i, value);
  }


  @Override
  public String toString() {
    return "<array: " + items.size() + ">";
  }

}
