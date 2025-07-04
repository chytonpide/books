package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/*
RUN:
cd /Users/kangwhiwon/IdeaProjects/books/Crafting\ Interpreters/lox/src/main/java/
javac com/craftinginterpreters/tool/GenerateAst.java
java com.craftinginterpreters.tool.GenerateAst "/Users/kangwhiwon/IdeaProjects/books/Crafting Interpreters/lox/src/main/java/com/craftinginterpreters/lox"
 */
public class GenerateAst {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output directory>");
      System.exit(64);
    }
    String outputDir = args[0];
    defineAst(outputDir, "Expr", Arrays.asList(
        "Assign   : Token name, Expr value",
        "Binary   : Expr left, Token operator, Expr right",
        "Call     : Expr callee, Token paren, List<Expr> arguments",
        "Get      : Expr object, Token name",
        "ArrayGet : Expr callee, Token square, Expr index",
        "Logical  : Expr left, Token operator, Expr right",
        "Set      : Expr object, Token name, Expr value",
        "ArraySet : Expr callee, Token square, Expr index, Expr value",
        "Super    : Token keyword, Token method",
        "This     : Token keyword",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Unary    : Token operator, Expr right",
        "Variable : Token name",
        "Array    : Token square, List<Expr> items"
    ));

    defineAst(outputDir, "Stmt", Arrays.asList(
        "Block      : List<Stmt> statements",
        "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
        "Expression : Expr expression",
        "Function   : Token name, List<Token> params, List<Stmt> body",
        "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
        "Print      : Expr expression",
        "Return     : Token keyword, Expr value",
        "Var        : Token name, Expr initializer",
        "While      : Expr condition, Stmt body"
    ));
  }

  private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, "UTF-8");

    writer.println("package com.craftinginterpreters.lox;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + baseName + " {");
    defineVisitor(writer, baseName, types);

    // The AST classes.
    for (String type : types) {
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();
      defineType(writer, baseName, className, fields);
    }

    // The base accept() method.
    writer.println();
    writer.println("  abstract <R> R accept(Visitor<R> visitor);");

    writer.println("}");
    writer.close();
  }

  private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
    writer.println("  interface Visitor<R> {");

    for (String type : types) {
      String typeName = type.split(":")[0].trim();
      writer.println("    R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
    }

    writer.println("  }");
  }

  private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
    writer.println("  static class " + className + " extends " + baseName + " {");

    // Constructor.
    writer.println("    " + className + "(" + fieldList + ") {");

    // Store parameters in fields.
    String[] fields = fieldList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("      this." + name + "=" + name + ";");
    }

    writer.println("    }");

    // Visitor pattern.
    writer.println();
    writer.println("    @Override");
    writer.println("    <R> R accept(Visitor<R> visitor) {");
    writer.println("      return visitor.visit" + className + baseName + "(this);");
    writer.println("    }");

    // Fields
    writer.println();
    for (String field : fields) {
      writer.println("    final " + field + ";");
    }

    writer.println("  }");
  }
}


