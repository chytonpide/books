package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.Expr.Array;
import com.craftinginterpreters.lox.Expr.ArrayGet;
import com.craftinginterpreters.lox.Expr.ArraySet;
import com.craftinginterpreters.lox.Expr.Assign;
import com.craftinginterpreters.lox.Expr.Binary;
import com.craftinginterpreters.lox.Expr.Call;
import com.craftinginterpreters.lox.Expr.Get;
import com.craftinginterpreters.lox.Expr.Grouping;
import com.craftinginterpreters.lox.Expr.Literal;
import com.craftinginterpreters.lox.Expr.Logical;
import com.craftinginterpreters.lox.Expr.Set;
import com.craftinginterpreters.lox.Expr.Super;
import com.craftinginterpreters.lox.Expr.This;
import com.craftinginterpreters.lox.Expr.Unary;
import com.craftinginterpreters.lox.Expr.Variable;

class AstPrinter implements Expr.Visitor<String> {

  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitAssignExpr(Assign expr) {
    return parenthesize(expr.name.lexeme + "=", expr.value);
  }

  @Override
  public String visitBinaryExpr(Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitCallExpr(Call expr) {
    // TODO
    String representation = this.print(expr.callee) + " - function-call";
    return parenthesize(representation);
  }

  @Override
  public String visitGetExpr(Get expr) {
    return "";
  }

  @Override
  public String visitArrayGetExpr(ArrayGet expr) {
    return "";
  }

  @Override
  public String visitLogicalExpr(Logical expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitSetExpr(Set expr) {
    return "";
  }

  @Override
  public String visitArraySetExpr(ArraySet expr) {
    return "";
  }

  @Override
  public String visitSuperExpr(Super expr) {
    return "";
  }

  @Override
  public String visitThisExpr(This expr) {
    return "";
  }

  @Override
  public String visitGroupingExpr(Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Literal expr) {
    if (expr.value == null) return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visitVariableExpr(Variable expr) {
    return parenthesize(expr.name.lexeme);
  }

  @Override
  public String visitArrayExpr(Array expr) {
    return "";
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr: exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  public static void main(String[] args) {
    Expr expression = new Expr.Binary(
        new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(new Expr.Literal(45.67)));

    /*
    AstPrinter printer = new AstPrinter();
    expression.accept(printer)
    위 코드보다 아래 코드가 조금 더 명확하다.
     */
    System.out.println(new AstPrinter().print(expression));
  }
}
