package transformer.expr.literal;

import abstractPattern.Action;
import ast.Expr;
import ast.StringLiteralExpr;
import org.javatuples.Pair;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StringLiteralTransform extends LiteralTransform{
    public StringLiteralTransform(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, StringLiteralExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    @Override
    public Pair<Expr, List<Pair<String, Expr>>> transform() {
        /* currently we do not have any pattern that will capture string literal */
        StringLiteralExpr retExpr = (StringLiteralExpr) this.originalExpr.treeCopy();
        List<Pair<String, Expr>> transformMap = new LinkedList<>();
        return new Pair<>(retExpr, transformMap);
    }

    @Override
    public boolean willBeTransformed() {
        return false;
    }

    @Override
    public Class<? extends Expr> correspondingAST() {
        return StringLiteralExpr.class;
    }
}
