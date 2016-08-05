package transformer.expr;

import abstractPattern.Action;
import ast.Expr;
import ast.LiteralExpr;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import transformer.expr.literal.LiteralTrans;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.List;

public abstract class ExprTrans {
    protected Expr originalNode = null;
    protected Namespace alterNamespace = null;
    protected RuntimeInfo runtimeInfo = null;
    protected Collection<Action> actions = null;

    public ExprTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, Expr astNodes) {
        this.originalNode = astNodes;
        this.alterNamespace = namespace;
        this.runtimeInfo = runtimeInfo;
        this.actions = actions;
    }

    public abstract boolean hasFutureTransform();

    public abstract Pair<Expr, List<Triplet<String, Expr, Boolean>>> transform();

    public abstract Class<? extends Expr> correspondAST();

    public static ExprTrans buildExprTransformer(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace namespace,
            Expr expr
    ) {
        /* TODO */
        if (expr instanceof LiteralExpr)
            return LiteralTrans.buildLiteralTransformer(actions, runtimeInfo, namespace, (LiteralExpr) expr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
