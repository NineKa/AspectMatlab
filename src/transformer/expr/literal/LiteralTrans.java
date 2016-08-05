package transformer.expr.literal;

import abstractPattern.Action;
import ast.FPLiteralExpr;
import ast.IntLiteralExpr;
import ast.LiteralExpr;
import ast.StringLiteralExpr;
import transformer.expr.ExprTrans;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;

public abstract class LiteralTrans extends ExprTrans{
    public LiteralTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, LiteralExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    public static LiteralTrans buildLiteralTransformer(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace namespace,
            LiteralExpr literalExpr
    ) {
        if (literalExpr instanceof IntLiteralExpr)
            return new IntLiteralTrans(actions, runtimeInfo, namespace, (IntLiteralExpr) literalExpr);
        if (literalExpr instanceof FPLiteralExpr)
            return new FPLiteralTrans(actions, runtimeInfo, namespace, (FPLiteralExpr) literalExpr);
        if (literalExpr instanceof StringLiteralExpr)
            return new StringLiteralTrans(actions, runtimeInfo, namespace, (StringLiteralExpr) literalExpr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
