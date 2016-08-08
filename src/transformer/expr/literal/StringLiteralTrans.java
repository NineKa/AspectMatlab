package transformer.expr.literal;

import ast.Expr;
import ast.Stmt;
import ast.StringLiteralExpr;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;

public final class StringLiteralTrans extends LiteralTrans {
    public StringLiteralTrans(ExprTransArgument argument, StringLiteralExpr stringLiteralExpr) {
        super(argument, stringLiteralExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        StringLiteralExpr copiedExpr = (StringLiteralExpr) this.originalNode.copy();
        return new Pair<>(copiedExpr, new LinkedList<>());
    }

    @Override
    public boolean hasTransformOnCurrentNode() {
        return false;
    }
}
