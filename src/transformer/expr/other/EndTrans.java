package transformer.expr.other;

import ast.EndExpr;
import ast.Expr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

import java.util.LinkedList;
import java.util.List;

public final class EndTrans extends ExprTrans {
    public EndTrans(TransformerArgument argument, EndExpr endExpr) {
        super(argument, endExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !hasTransformOnCurrentNode();
        if (this.endExpressionResolveMap.keySet().contains(originalNode)) {
            Expr retExpr = this.endExpressionResolveMap.get(originalNode).treeCopy();
            return new Pair<>(retExpr, new LinkedList<>());
        } else {
            return new Pair<>(new EndExpr(), new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        if (this.endExpressionResolveMap.keySet().contains(originalNode)) return true;
        return false;
    }
}
