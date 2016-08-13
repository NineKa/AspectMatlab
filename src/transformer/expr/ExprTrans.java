package transformer.expr;

import abstractPattern.Action;
import abstractPattern.Primitive;
import ast.*;
import org.javatuples.Pair;
import transformer.expr.binary.BinaryTrans;
import transformer.expr.literal.LiteralTrans;
import transformer.expr.unary.UnaryTrans;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ExprTrans {
    protected Expr originalNode = null;
    protected RuntimeInfo runtimeInfo = null;
    protected Collection<Action> actions = null;
    protected Namespace alterNamespace = null;
    protected Function<ASTNode, Boolean> ignoreDelegate = null;
    protected Consumer<Stmt> jointPointDelegate = null;

    @Deprecated
    protected ExprTransArgument originalArgument = null;

    @SuppressWarnings("deprecation")
    public ExprTrans (ExprTransArgument argument, Expr expr) {
        this.actions            = argument.actions;
        this.runtimeInfo        = argument.runtimeInfo;
        this.alterNamespace     = argument.alterNamespace;
        this.ignoreDelegate     = argument.ignoreDelegate;
        this.jointPointDelegate = argument.jointPointDelegate;
        this.originalArgument   = argument;

        this.originalNode       = expr;
    }

    public abstract Pair<Expr, List<Stmt>> copyAndTransform();

    public abstract boolean hasFurtherTransform();

    public boolean hasTransformOnCurrentNode() {
        if (ignoreDelegate.apply(originalNode) == true) return false;
        for (Action action : actions) {
            assert action.getPattern() instanceof Primitive;
            Primitive pattern = (Primitive) action.getPattern();
            IsPossibleJointPointResult query = pattern.isPossibleJointPoint(originalNode, runtimeInfo);
            if (query.isPossible()) return true;
        }
        return false;
    }

    public Class<? extends Expr> getASTNodeClass() {
        return originalNode.getClass();
    }

    public static ExprTrans buildExprTransformer(ExprTransArgument argument, Expr expr) {
        if (expr instanceof LiteralExpr) return LiteralTrans.buildLiteralTransformer(argument, (LiteralExpr) expr);
        if (expr instanceof UnaryExpr) return UnaryTrans.buildUnaryTransformer(argument, (UnaryExpr) expr);
        if (expr instanceof BinaryExpr) return BinaryTrans.buildBinaryTransformer(argument, (BinaryExpr) expr);

        /* control flow should not reach here */
        throw new AssertionError();
    }
}
