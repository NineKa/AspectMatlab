package matcher;

import ast.EQExpr;
import ast.Expr;
import ast.IntLiteralExpr;
import natlab.DecIntNumericLiteralValue;

public class DimAlphabetCompareFunc extends AbstractAlphabetCompareFunc<Integer>{
    public Expr getCompareFunc(Expr pCompareIter, Integer pCompareTarget) {
        /* [pCompareIter] == [pCompareTarget] */
        EQExpr returnExpr = new EQExpr();
        returnExpr.setLHS(pCompareIter.treeCopy());
        returnExpr.setRHS(new IntLiteralExpr(new DecIntNumericLiteralValue(pCompareTarget.toString())));
        return returnExpr;
    }

    @Override public String toString() {
        return "[Compare Iterator] == [Compare Target]";
    }
}
