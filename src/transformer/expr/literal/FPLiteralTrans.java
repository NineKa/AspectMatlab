package transformer.expr.literal;

import abstractPattern.Action;
import ast.Expr;
import ast.FPLiteralExpr;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class FPLiteralTrans extends LiteralTrans {

    public FPLiteralTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, FPLiteralExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    @Override
    public boolean hasFutureTransform() {
        return false;   /* obviously */
    }

    @Override
    public Pair<Expr, List<Triplet<String, Expr, Boolean>>> transform() {
        /* currently we do not have a pattern to match floating point literals */
        FPLiteralExpr expr = (FPLiteralExpr) this.originalNode.treeCopy();
        List<Triplet<String, Expr, Boolean>> transformMap = new LinkedList<>();
        return new Pair<>(expr, transformMap);
    }

    @Override
    public Class<? extends Expr> correspondAST() {
        return FPLiteralExpr.class;
    }

}
