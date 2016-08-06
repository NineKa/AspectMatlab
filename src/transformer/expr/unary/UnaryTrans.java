package transformer.expr.unary;

import abstractPattern.Action;
import ast.*;
import transformer.expr.ExprTrans;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;

public abstract class UnaryTrans extends ExprTrans {

    protected Expr operandExpr = null;
    protected ExprTrans operandTransformer = null;

    public UnaryTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, UnaryExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
        this.operandExpr = expr.getOperand();
        this.operandTransformer = ExprTrans.buildExprTransformer(actions, runtimeInfo, namespace, expr);
    }

    public static UnaryTrans buildUnaryTransformer(
            Collection<Action> actions,
            RuntimeInfo runtimeInfo,
            Namespace namespace,
            UnaryExpr unaryExpr
    ) {
        if (unaryExpr instanceof UMinusExpr)
            return new UMinusTrans(actions, runtimeInfo, namespace, (UMinusExpr) unaryExpr);
        if (unaryExpr instanceof UPlusExpr)
            return new UPlusTrans(actions, runtimeInfo, namespace, (UPlusExpr) unaryExpr);
        if (unaryExpr instanceof NotExpr)
            return new NotTrans(actions, runtimeInfo, namespace, (NotExpr) unaryExpr);
        if (unaryExpr instanceof MTransposeExpr)
            return new MTransposeTrans(actions, runtimeInfo, namespace, (MTransposeExpr) unaryExpr);
        if (unaryExpr instanceof ArrayTransposeExpr)
            return new ArrayTransposeTrans(actions, runtimeInfo, namespace, (ArrayTransposeExpr) unaryExpr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
