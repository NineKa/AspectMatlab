package transformer.expr.lvalue;

import ast.LValueExpr;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

public abstract class LValueTrans extends ExprTrans{
    public LValueTrans(TransformerArgument argument, LValueExpr lValueExpr) {
        super(argument, lValueExpr);
    }

    public static LValueTrans buildLValueTransformer(TransformerArgument argument, LValueExpr lValueExpr) {


        /* control flow should not reach here */
        throw new AssertionError();
    }
}
