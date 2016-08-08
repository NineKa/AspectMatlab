package transformer.expr.literal;

import ast.Expr;
import ast.IntLiteralExpr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;

public final class IntLiteralTrans extends LiteralTrans {
    public IntLiteralTrans(ExprTransArgument argument, IntLiteralExpr intLiteralExpr) {
        super(argument, intLiteralExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        IntLiteralExpr copiedExpr = (IntLiteralExpr) this.originalNode.copy();
        return new Pair<>(copiedExpr, new LinkedList<>());
    }

    @Override
    public boolean hasTransformOnCurrentNode() {
        return false;
    }
}
