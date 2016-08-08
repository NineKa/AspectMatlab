package transformer.expr.unary;

import ast.Expr;
import ast.Stmt;
import ast.UMinusExpr;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.List;

public final class UMinusTrans extends UnaryTrans {
    public UMinusTrans(ExprTransArgument argument, UMinusExpr uMinusExpr) {
        super(argument, uMinusExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert this.hasTransformOnCurrentNode() == false;
        UMinusExpr copiedNode = (UMinusExpr) this.originalNode.copy();
        Pair<Expr, List<Stmt>> operandTransformResult = this.operandTransformer.copyAndTransform();
        Expr copiedOperand = operandTransformResult.getValue0();
        copiedOperand.setParent(copiedNode);
        copiedNode.setOperand(copiedOperand);
        return new Pair<>(copiedNode, operandTransformResult.getValue1());
    }
}
