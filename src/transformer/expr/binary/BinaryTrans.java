package transformer.expr.binary;

import abstractPattern.Action;
import ast.BinaryExpr;
import ast.Expr;
import transformer.expr.ExprTrans;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;

public abstract class BinaryTrans extends ExprTrans {

    protected Expr lhsExpr = null;
    protected Expr rhsExpr = null;

    protected ExprTrans lhsTransformer = null;
    protected ExprTrans rhsTransformer = null;

    public BinaryTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, BinaryExpr expr) {
        super(actions, runtimeInfo, namespace, expr);

        this.lhsExpr = expr.getLHS();
        this.rhsExpr = expr.getRHS();
        this.lhsTransformer = ExprTrans.buildExprTransformer(actions, runtimeInfo, namespace, this.lhsExpr);
        this.rhsTransformer = ExprTrans.buildExprTransformer(actions, runtimeInfo, namespace, this.rhsExpr);
    }

    public static BinaryTrans buildBianryTransformer(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace namespace,
            BinaryExpr expr
    ) {
        /* TODO */

        /* control flow should not reach here */
        throw new AssertionError();
    }

}
