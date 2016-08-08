package transformer.expr.unary;

import ast.Expr;
import ast.NotExpr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.List;

public final class NotTrans extends UnaryTrans {
    public NotTrans(ExprTransArgument argument, NotExpr notExpr) {
        super(argument, notExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert this.hasTransformOnCurrentNode() == false;
        NotExpr copiedNode = (NotExpr)this.originalNode.copy();
        Pair<Expr, List<Stmt>> operandTransformResult = this.operandTransformer.copyAndTransform();
        Expr copiedOperand = operandTransformResult.getValue0();
        copiedOperand.setParent(copiedNode);
        copiedNode.setOperand(copiedOperand);
        return new Pair<>(copiedNode, operandTransformResult.getValue1());
    }
}
