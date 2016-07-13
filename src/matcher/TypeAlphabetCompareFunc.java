package matcher;

import ast.*;

public class TypeAlphabetCompareFunc extends AbstractAlphabetCompareFunc<String> {
    public Expr getCompareFunc(Expr pCompareIter, String pCompareTarget) {
        /* isa([pCompareIter], [pCompareTarget]) */
        ParameterizedExpr returnExpr = new ParameterizedExpr();
        returnExpr.setTarget(new NameExpr(new Name("isa")));
        returnExpr.addArg(pCompareIter.treeCopy());
        returnExpr.addArg(new StringLiteralExpr(pCompareTarget));
        return returnExpr;
    }

    @Override public String toString() {
        return "isa([Compare Iterator], [Compare Target])";
    }
}
