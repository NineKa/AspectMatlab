package transformer.expr;

import abstractPattern.Action;
import abstractPattern.Primitive;
import abstractPattern.analysis.PatternType;
import ast.*;
import org.javatuples.Pair;
import transformer.expr.binary.BinaryTrans;
import transformer.expr.literal.LiteralTrans;
import transformer.expr.lvalue.LValueTrans;
import transformer.expr.other.*;
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
    protected Consumer<Pair<Stmt, PatternType>> jointPointDelegate = null;

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
        if (expr instanceof LValueExpr) return LValueTrans.buildLValueTransformer(argument, (LValueExpr) expr);

        if (expr instanceof CellArrayExpr) return new CellArrayTrans(argument, (CellArrayExpr) expr);
        if (expr instanceof ColonExpr) return new ColonTrans(argument, (ColonExpr) expr);
        if (expr instanceof EndExpr) return new EndTrans(argument, (EndExpr) expr);
        if (expr instanceof FunctionHandleExpr) return new FunctionHandleTrans(argument, (FunctionHandleExpr) expr);
        if (expr instanceof LambdaExpr) return new LambdaTrans(argument, (LambdaExpr) expr);
        if (expr instanceof SuperClassMethodExpr) return new SuperClassMethodTrans(argument, (SuperClassMethodExpr) expr);
        if (expr instanceof RangeExpr) return new RangeTrans(argument, (RangeExpr) expr);
        /* control flow should not reach here */
        throw new AssertionError();
    }
}
