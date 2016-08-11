package transformer.expr.binary;

import ast.*;
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

        if (binaryExpr instanceof MTimesExpr) return new MTimesTrans(argument, (MTimesExpr) binaryExpr);
        if (binaryExpr instanceof MDivExpr) return new MDivTrans(argument, (MDivExpr) binaryExpr);
        if (binaryExpr instanceof MLDivExpr) return new MLDivTrans(argument, (MLDivExpr) binaryExpr);
        if (binaryExpr instanceof MPowExpr) return new MPowTrans(argument, (MPowExpr) binaryExpr);

        if (binaryExpr instanceof ETimesExpr) return new ETimesTrans(argument, (ETimesExpr) binaryExpr);
        if (binaryExpr instanceof EDivExpr) return new EDivTrans(argument, (EDivExpr) binaryExpr);
        if (binaryExpr instanceof ELDivExpr) return new ELDivTrans(argument, (ELDivExpr) binaryExpr);
        if (binaryExpr instanceof EPowExpr) return new EPowTrans(argument, (EPowExpr) binaryExpr);

        if (binaryExpr instanceof AndExpr) return new AndTrans(argument, (AndExpr) binaryExpr);
        if (binaryExpr instanceof OrExpr) return new OrTrans(argument, (OrExpr) binaryExpr);
        if (binaryExpr instanceof LTExpr) return new LTTrans(argument, (LTExpr) binaryExpr);
        if (binaryExpr instanceof GTExpr) return new GTTrans(argument, (GTExpr) binaryExpr);
        if (binaryExpr instanceof LEExpr) return new LETrans(argument, (LEExpr) binaryExpr);
        if (binaryExpr instanceof GEExpr) return new GETrans(argument, (GEExpr) binaryExpr);
        if (binaryExpr instanceof EQExpr) return new EQTrans(argument, (EQExpr) binaryExpr);
        if (binaryExpr instanceof NEExpr) return new NETrans(argument, (NEExpr) binaryExpr);
        /* control flow should not reach here */
        throw new AssertionError();
    }
}
