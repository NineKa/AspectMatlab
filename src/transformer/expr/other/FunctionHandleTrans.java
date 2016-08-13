package transformer.expr.other;

import ast.Expr;
import ast.FunctionHandleExpr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;

public final class FunctionHandleTrans extends ExprTrans {
    public FunctionHandleTrans(ExprTransArgument argument, FunctionHandleExpr functionHandleExpr) {
        super(argument, functionHandleExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !hasTransformOnCurrentNode();
        FunctionHandleExpr copiedExpr = (FunctionHandleExpr) this.originalNode.treeCopy();
        return new Pair<>(copiedExpr, new LinkedList<>());
    }

    @Override
    public boolean hasFurtherTransform() {
        return false;
    }
}
