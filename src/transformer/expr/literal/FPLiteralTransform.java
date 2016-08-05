package transformer.expr.literal;

import abstractPattern.Action;
import ast.Expr;
import ast.FPLiteralExpr;
import org.javatuples.Pair;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FPLiteralTransform extends LiteralTransform{
    public FPLiteralTransform(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, FPLiteralExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    @Override
    public Pair<Expr, List<Pair<String, Expr>>> transform() {
        /* currently we do not have any pattern that will capture floating point literal */
        FPLiteralExpr retExpr = (FPLiteralExpr) this.originalExpr.treeCopy();
        List<Pair<String, Expr>> transformMap = new LinkedList<>();
        return new Pair<>(retExpr, transformMap);
    }

    @Override
    public boolean willBeTransformed() {
        return false;
    }

    @Override
    public Class<? extends Expr> correspondingAST() {
        return FPLiteralExpr.class;
    }
}
