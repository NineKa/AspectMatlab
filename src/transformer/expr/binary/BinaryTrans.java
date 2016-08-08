package transformer.expr.binary;

import ast.BinaryExpr;
import ast.Expr;
import ast.MinusExpr;
import ast.PlusExpr;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;

public abstract class BinaryTrans extends ExprTrans {
    protected Expr originalLHS = null;
    protected Expr originalRHS = null;
    protected ExprTrans LHSTransformer = null;
    protected ExprTrans RHSTransformer = null;

    public BinaryTrans(ExprTransArgument argument, BinaryExpr binaryExpr) {
        super(argument, binaryExpr);
        this.originalLHS = binaryExpr.getLHS();
        this.originalRHS = binaryExpr.getRHS();

        this.LHSTransformer = ExprTrans.buildExprTransformer(argument, this.originalLHS);
        this.RHSTransformer = ExprTrans.buildExprTransformer(argument, this.originalRHS);
    }

    @Override
    public boolean hasFurtherTransform() {
        if (this.hasTransformOnCurrentNode()) return true;
        boolean transformOnLHS = this.LHSTransformer.hasFurtherTransform();
        boolean transformOnRHS = this.RHSTransformer.hasFurtherTransform();
        return transformOnLHS || transformOnRHS;
    }

    public static BinaryTrans buildBinaryTransformer(ExprTransArgument argument, BinaryExpr binaryExpr) {
        if (binaryExpr instanceof PlusExpr) return new PlusTrans(argument, (PlusExpr) binaryExpr);
        if (binaryExpr instanceof MinusExpr) return new MinusTrans(argument, (MinusExpr) binaryExpr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
