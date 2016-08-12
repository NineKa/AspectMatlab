package debug;

import matcher.parameter.mex.cAST.*;

public class cASTDebug {
    public static void main(String argv[]) {
        Function foo = new Function();
        foo.setName("foo");
        foo.setType("int");
        foo.addSignature(new Signature("int[10]", "arr"));
        foo.addSignature(new Signature("bool", "flag"));

        IfStmt ifStmt = new IfStmt();
        ifStmt.setCondition(new NameExpr(new Name("flag")));


        ArrayIndexExpr indexExpr = new ArrayIndexExpr();
        indexExpr.setTarget(new NameExpr(new Name("arr")));
        indexExpr.setIndex(new IntegerConstantExpr(0));

        ReturnStmt returnStmt = new ReturnStmt();
        returnStmt.setExpression(indexExpr);

        ArrayIndexExpr indexExpr2 = new ArrayIndexExpr();
        indexExpr2.setTarget(new NameExpr(new Name("arr")));
        indexExpr2.setIndex(new IntegerConstantExpr(1));

        ReturnStmt returnStmt2 = new ReturnStmt();
        returnStmt2.setExpression(indexExpr2);

        ifStmt.addStatement(returnStmt);
        ifStmt.addElseStatement(returnStmt2);

        foo.addStatement(ifStmt);

        System.out.println(foo.getPrettyPrinted());
    }
}
