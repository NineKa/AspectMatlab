package transformer.expr.unary;

import abstractPattern.Action;
import ast.Expr;
import ast.UMinusExpr;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class UMinusTrans extends UnaryTrans {

    public UMinusTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, UMinusExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    @Override
    public boolean hasFutureTransform() {
        /* currently we do not have a pattern to capture unary minus */
        return this.operandTransformer.hasFutureTransform();
    }

    @Override
    public Pair<Expr, List<Triplet<String, Expr, Boolean>>> transform() {
        UMinusExpr uMinusExpr = null;
        List<Triplet<String, Expr, Boolean>> transformMap = null;
        if (this.operandTransformer.hasFutureTransform()) {
            uMinusExpr = (UMinusExpr) this.originalNode.treeCopy();
            Pair<Expr, List<Triplet<String, Expr, Boolean>>> result = this.operandTransformer.transform();
            uMinusExpr.setOperand(result.getValue0());
            transformMap = result.getValue1();
        } else {
            uMinusExpr = (UMinusExpr) this.originalNode.treeCopy();
            transformMap = new LinkedList<>();
        }
        assert uMinusExpr != null && transformMap != null;
        return new Pair<>(uMinusExpr, transformMap);
    }

    @Override
    public Class<? extends Expr> correspondAST() {
        return UMinusExpr.class;
    }

}
