package transformer.expr.other;

import ast.ColonExpr;
import ast.Expr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

import java.util.LinkedList;
import java.util.List;

public final class ColonTrans extends ExprTrans {
    public ColonTrans(TransformerArgument argument, ColonExpr expr) {
        super(argument, expr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !this.hasTransformOnCurrentNode();
        ColonExpr copiedNode = (ColonExpr) this.originalNode.copy();
        return new Pair<>(copiedNode, new LinkedList<>());
    }

    @Override
    public boolean hasFurtherTransform() {
        return false;
    }
}
