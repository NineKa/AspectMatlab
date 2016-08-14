package transformer.expr.lvalue;

import ast.LValueExpr;
import ast.NameExpr;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;

public abstract class LValueTrans extends ExprTrans{
    public LValueTrans(ExprTransArgument argument, LValueExpr lValueExpr) {
        super(argument, lValueExpr);
    }

    public static LValueTrans buildLValueTransformer(ExprTransArgument argument, LValueExpr lValueExpr) {
        if (lValueExpr instanceof NameExpr) return new NameTrans(argument, (NameExpr) lValueExpr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
