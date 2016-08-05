package transformer.expr.literal;

import abstractPattern.Action;
import ast.Expr;
import ast.IntLiteralExpr;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class IntLiteralTrans extends LiteralTrans {

    public IntLiteralTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, IntLiteralExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    @Override
    public boolean hasFutureTransform() {
        return false;   /* obviously */
    }

    @Override
    public Pair<Expr, List<Triplet<String, Expr, Boolean>>> transform() {
        /* currently we do not have a pattern to match int literals */
        IntLiteralExpr expr = (IntLiteralExpr) this.originalNode.treeCopy();
        List<Triplet<String, Expr, Boolean>> transferMap = new LinkedList<>();
        return new Pair<>(expr, transferMap);
    }

    @Override
    public Class<? extends Expr> correspondAST() {
        return IntLiteralExpr.class;
    }

}
