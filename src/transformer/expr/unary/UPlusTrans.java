package transformer.expr.unary;

import abstractPattern.Action;
import ast.Expr;
import ast.UPlusExpr;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class UPlusTrans extends UnaryTrans {

    public UPlusTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, UPlusExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    @Override
    public boolean hasFutureTransform() {
        /* currently we do not have a pattern to capture unary plus */
        return this.operandTransformer.hasFutureTransform();
    }

    @Override
    public Pair<Expr, List<Triplet<String, Expr, Boolean>>> transform() {
        UPlusExpr uPlusExpr = null;
        List<Triplet<String, Expr, Boolean>> transformMap = null;
        if (this.operandTransformer.hasFutureTransform()) {
            uPlusExpr = (UPlusExpr) this.originalNode.treeCopy();
            Pair<Expr, List<Triplet<String, Expr, Boolean>>> result = this.operandTransformer.transform();
            uPlusExpr.setOperand(result.getValue0());
            transformMap = result.getValue1();
        } else {
            uPlusExpr = (UPlusExpr) this.originalNode.treeCopy();
            transformMap = new LinkedList<>();
        }
        assert uPlusExpr != null && transformMap != null;
        return new Pair<>(uPlusExpr, transformMap);
    }

    @Override
    public Class<? extends Expr> correspondAST() {
        return UPlusExpr.class;
    }

}
