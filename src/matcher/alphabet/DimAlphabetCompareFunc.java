package matcher.alphabet;

import ast.EQExpr;
import ast.Expr;
import ast.IntLiteralExpr;
import natlab.DecIntNumericLiteralValue;

import java.util.function.BiFunction;

public class DimAlphabetCompareFunc implements BiFunction<Expr, Integer, Expr> {
    public Expr apply(Expr pCompareIter, Integer pCompareTarget) {
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
