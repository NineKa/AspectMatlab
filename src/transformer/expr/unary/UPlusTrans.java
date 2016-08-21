package transformer.expr.unary;

import ast.Expr;
import ast.Stmt;
import ast.UPlusExpr;
import org.javatuples.Pair;
import transformer.TransformerArgument;

import java.util.List;

public final class UPlusTrans extends UnaryTrans {
    public UPlusTrans (TransformerArgument argument, UPlusExpr uPlusExpr) {
        super(argument, uPlusExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert this.hasTransformOnCurrentNode() == false;
        UPlusExpr copiedNode = (UPlusExpr) this.originalNode.copy();
        Pair<Expr, List<Stmt>> operandTransformResult = this.operandTransformer.copyAndTransform();
        Expr copiedOperand = operandTransformResult.getValue0();
        copiedOperand.setParent(copiedNode);
        copiedNode.setOperand(copiedOperand);
        return new Pair<>(copiedNode, operandTransformResult.getValue1());
    }
}
