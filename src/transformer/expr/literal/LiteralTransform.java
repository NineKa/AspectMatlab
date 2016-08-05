package transformer.expr.literal;

import abstractPattern.Action;
import ast.FPLiteralExpr;
import ast.IntLiteralExpr;
import ast.LiteralExpr;
import ast.StringLiteralExpr;
import transformer.expr.ExprTransfrom;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;

public abstract class LiteralTransform extends ExprTransfrom{
    public LiteralTransform(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, LiteralExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    public static LiteralTransform buildLiteralTransform(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace namespace,
            LiteralExpr expr
    ) {
        assert expr instanceof LiteralExpr;
        if (expr instanceof IntLiteralExpr)
            return new IntLiteralTransform(actions, runtimeInfo, namespace, (IntLiteralExpr) expr);
        if (expr instanceof FPLiteralExpr)
            return new FPLiteralTransform(actions, runtimeInfo, namespace, (FPLiteralExpr) expr);
        if (expr instanceof StringLiteralExpr)
            return new StringLiteralTransform(actions, runtimeInfo, namespace, (StringLiteralExpr) expr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
