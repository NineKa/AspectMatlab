package transformer.expr.lvalue;

import ast.*;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

public abstract class LValueTrans extends ExprTrans{
    public LValueTrans(TransformerArgument argument, LValueExpr lValueExpr) {
        super(argument, lValueExpr);
    }

    public static LValueTrans buildLValueTransformer(TransformerArgument argument, LValueExpr lValueExpr) {
        if (lValueExpr instanceof CellIndexExpr) return new CellIndexTrans(argument, (CellIndexExpr) lValueExpr);
        if (lValueExpr instanceof NameExpr) return new NameTrans(argument, (NameExpr) lValueExpr);
        if (lValueExpr instanceof ParameterizedExpr) return new ParametrizedTrans(argument, (ParameterizedExpr) lValueExpr);
        if (lValueExpr instanceof DotExpr) return new DotTrans(argument, (DotExpr) lValueExpr);
        if (lValueExpr instanceof MatrixExpr) return new MatrixTrans(argument, (MatrixExpr) lValueExpr);
        /* control flow should not reach here */
        throw new AssertionError();
    }
}
