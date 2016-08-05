package transformer.expr.unary;

import abstractPattern.Action;
import ast.Expr;
import ast.NotExpr;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class NotTrans extends UnaryTrans {

    public NotTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, NotExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    @Override
    public boolean hasFutureTransform() {
        /* currently we do not have a pattern to capture not operation */
        return this.operandTransformer.hasFutureTransform();
    }

    @Override
    public Pair<Expr, List<Triplet<String, Expr, Boolean>>> transform() {
        NotExpr notExpr = null;
        List<Triplet<String, Expr, Boolean>> transformMap = null;
        if (this.operandTransformer.hasFutureTransform()) {
            notExpr = (NotExpr) this.originalNode.treeCopy();
            Pair<Expr, List<Triplet<String, Expr, Boolean>>> result = this.operandTransformer.transform();
            notExpr.setOperand(result.getValue0());
            transformMap = result.getValue1();
        } else {
            notExpr = (NotExpr) this.originalNode.treeCopy();
            transformMap = new LinkedList<>();
        }
        assert notExpr != null && transformMap != null;
        return new Pair<>(notExpr, transformMap);
    }

    @Override
    public Class<? extends Expr> correspondAST() {
        return NotExpr.class;
    }

}
