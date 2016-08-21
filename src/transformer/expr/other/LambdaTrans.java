package transformer.expr.other;

import ast.Expr;
import ast.LambdaExpr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

import java.util.LinkedList;
import java.util.List;

public final class LambdaTrans extends ExprTrans{
    public LambdaTrans(TransformerArgument argument, LambdaExpr lambdaExpr) {
        super(argument, lambdaExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !this.hasTransformOnCurrentNode();
        LambdaExpr copiedNode = (LambdaExpr) this.originalNode.copy();
        return new Pair<>(copiedNode, new LinkedList<>());
    }

    @Override
    public boolean hasFurtherTransform() {
        return false;
    }
}
