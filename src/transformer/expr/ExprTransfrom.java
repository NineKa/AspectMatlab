package transformer.expr;

import abstractPattern.Action;
import ast.Expr;
import ast.LiteralExpr;
import org.javatuples.Pair;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class ExprTransfrom {
    /* Abstract Class of a expression transformer */

    protected Collection<Action> actions = new HashSet<>();
    protected Expr originalExpr = null;
    protected RuntimeInfo runtimeInfo = null;
    protected Namespace namespace = null;

    public ExprTransfrom(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, Expr expr) {
        this.actions = actions;
        this.originalExpr = expr;
        this.runtimeInfo = runtimeInfo;
        this.namespace = namespace;
    }

    public abstract Pair<Expr, List<Pair<String, Expr>>> transform();

    public abstract boolean willBeTransformed();

    public abstract Class<? extends Expr> correspondingAST();

    public ExprTransfrom buildTransformer(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace namespace,
            Expr expr
    ) {
        if (expr instanceof LiteralExpr) return buildTransformer(actions, runtimeInfo, namespace, expr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
