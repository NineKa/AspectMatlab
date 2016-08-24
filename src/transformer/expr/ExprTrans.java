package transformer.expr;

import abstractPattern.Action;
import abstractPattern.Primitive;
import ast.*;
import org.javatuples.Pair;
import transformer.Transformer;
import transformer.TransformerArgument;
import transformer.expr.binary.BinaryTrans;
import transformer.expr.literal.LiteralTrans;
import transformer.expr.lvalue.LValueTrans;
import transformer.expr.other.*;
import transformer.expr.unary.UnaryTrans;
import transformer.jointpoint.AMJointPoint;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ExprTrans implements Transformer<Expr>{
    protected Expr originalNode = null;
    protected RuntimeInfo runtimeInfo = null;
    protected Collection<Action> actions = null;
    protected Namespace alterNamespace = null;
    protected Function<ASTNode, Boolean> ignoreDelegate = null;
    protected Consumer<AMJointPoint> jointPointDelegate = null;

    protected Map<EndExpr, ParameterizedExpr> endExpressionResolveMap = null;
    protected String enclosingFilename = null;

    @Deprecated
    protected TransformerArgument originalArgument = null;

    @Deprecated
    protected Stack<AssignStmt> assignRetriveStack = null;

    @SuppressWarnings("deprecation")
    public ExprTrans (TransformerArgument argument, Expr expr) {
        this.actions            = argument.actions;
        this.runtimeInfo        = argument.runtimeInfo;
        this.alterNamespace     = argument.alterNamespace;
        this.ignoreDelegate     = argument.ignoreDelegate;
        this.jointPointDelegate = argument.jointPointDelegate;
        this.originalArgument   = argument;

        this.endExpressionResolveMap = argument.endExpressionResolveMap;
        this.enclosingFilename  = argument.enclosingFilename;

        this.originalNode       = expr;

        this.assignRetriveStack   = argument.assignRetriveStack;
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

    public Collection<Action> getPossibleAttachedActionsSet() {
        if (ignoreDelegate.apply(originalNode) == true) return new HashSet<>();
        Collection<Action> returnSet = new HashSet<>();
        for (Action action : actions) {
            assert action.getPattern() instanceof Primitive;
            Primitive pattern = (Primitive) action.getPattern();
            IsPossibleJointPointResult query = pattern.isPossibleJointPoint(originalNode, runtimeInfo);
            if (query.isPossible()) returnSet.add(action);
        }
        return returnSet;
    }

    public Class<? extends Expr> getASTNodeClass() {
        return originalNode.getClass();
    }

    public static ExprTrans buildExprTransformer(TransformerArgument argument, Expr expr) {
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

    @Deprecated
    @SuppressWarnings("deprecation")
    public TransformerArgument getTransformArgument() {
        return this.originalArgument;
    }
}
