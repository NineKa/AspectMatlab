package transformer.expr.unary;

import ast.*;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

public abstract class UnaryTrans extends ExprTrans {
    protected Expr originalOperand = null;
    protected ExprTrans operandTransformer = null;

    public UnaryTrans (TransformerArgument argument, UnaryExpr unaryExpr) {
        super(argument, unaryExpr);
        this.originalOperand = unaryExpr.getOperand();
        this.operandTransformer = ExprTrans.buildExprTransformer(argument, this.originalOperand);
    }

    @Override
    public boolean hasFurtherTransform() {
        if (this.hasTransformOnCurrentNode()) return true;
        return this.operandTransformer.hasFurtherTransform();
    }

    public static UnaryTrans buildUnaryTransformer(TransformerArgument argument, UnaryExpr unaryExpr) {
        if (unaryExpr instanceof UMinusExpr) return new UMinusTrans(argument, (UMinusExpr) unaryExpr);
        if (unaryExpr instanceof UPlusExpr) return new UPlusTrans(argument, (UPlusExpr) unaryExpr);
        if (unaryExpr instanceof NotExpr) return new NotTrans(argument, (NotExpr) unaryExpr);
        if (unaryExpr instanceof MTransposeExpr) return new MTransposeTrans(argument, (MTransposeExpr) unaryExpr);
        if (unaryExpr instanceof ArrayTransposeExpr) return new ArrayTransposeTrans(argument, (ArrayTransposeExpr) unaryExpr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
