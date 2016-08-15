package transformer.expr.lvalue;

import ast.DotExpr;
import ast.Expr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;

import java.util.List;

public final class DotTrans extends LValueTrans {
    public ExprTrans targetTrans = null;

    public DotTrans(ExprTransArgument argument, DotExpr dotExpr) {
        super(argument, dotExpr);
        targetTrans = ExprTrans.buildExprTransformer(argument, dotExpr.getTarget());
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert originalNode instanceof DotExpr;
        Pair<Expr, List<Stmt>> targetTransformResult = targetTrans.copyAndTransform();

        DotExpr retExpr = new DotExpr();
        retExpr.setTarget(targetTransformResult.getValue0());
        retExpr.setField(((DotExpr) originalNode).getField());

        return new Pair<>(retExpr, targetTransformResult.getValue1());
    }

    @Override
    public boolean hasFurtherTransform() {
        assert !hasTransformOnCurrentNode();
        return targetTrans.hasFurtherTransform();
    }
}
