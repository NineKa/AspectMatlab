package transformer.expr.other;

import ast.Expr;
import ast.Stmt;
import ast.SuperClassMethodExpr;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

import java.util.LinkedList;
import java.util.List;

public final class SuperClassMethodTrans extends ExprTrans {
    public SuperClassMethodTrans(TransformerArgument argument, SuperClassMethodExpr superClassMethodExpr) {
        super(argument, superClassMethodExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !this.hasTransformOnCurrentNode();
        SuperClassMethodExpr superClassMethodExpr = (SuperClassMethodExpr) this.originalNode.copy();
        return new Pair<>(superClassMethodExpr, new LinkedList<>());
    }

    @Override
    public boolean hasFurtherTransform() {
        return false;
    }
}
