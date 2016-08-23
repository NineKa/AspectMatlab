package transformer.expr.lvalue;

import ast.CellIndexExpr;
import ast.LValueExpr;
import ast.NameExpr;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

public abstract class LValueTrans extends ExprTrans{
    public LValueTrans(TransformerArgument argument, LValueExpr lValueExpr) {
        super(argument, lValueExpr);
    }

    public static LValueTrans buildLValueTransformer(TransformerArgument argument, LValueExpr lValueExpr) {
        if (lValueExpr instanceof NameExpr) return new NameTrans(argument, (NameExpr) lValueExpr);
        if (lValueExpr instanceof CellIndexExpr) return new CellIndexTrans(argument, (CellIndexExpr) lValueExpr);
        /* control flow should not reach here */
        throw new AssertionError();
    }
}
