package transformer.expr.literal;

import ast.Expr;
import ast.FPLiteralExpr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.TransformerArgument;

import java.util.LinkedList;
import java.util.List;

public final class FPLiteralTrans extends LiteralTrans {
    public FPLiteralTrans(TransformerArgument argument, FPLiteralExpr fpLiteralExpr) {
        super(argument, fpLiteralExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        FPLiteralExpr copiedExpr = (FPLiteralExpr) this.originalNode.treeCopy();
        return new Pair<>(copiedExpr, new LinkedList<>());
    }

    @Override
    public boolean hasTransformOnCurrentNode() {
        return false;
    }
}
