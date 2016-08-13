package transformer.expr.other;

import ast.EndExpr;
import ast.Expr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;

public final class EndTrans extends ExprTrans {
    public EndTrans(ExprTransArgument argument, EndExpr endExpr) {
        super(argument, endExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !hasTransformOnCurrentNode();
        EndExpr copiedNode = (EndExpr) this.originalNode.copy();
        return new Pair<>(copiedNode, new LinkedList<>());
    }

    @Override
    public boolean hasFurtherTransform() {
        return false;
    }
}
