package transformer.expr.binary;

import ast.AndExpr;
import ast.Expr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;

public final class AndTrans extends BinaryTrans {
    public AndTrans(ExprTransArgument argument, AndExpr binaryExpr) {
        super(argument, binaryExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !this.hasTransformOnCurrentNode();
        Pair<Expr, List<Stmt>> lhsTransformResult = this.LHSTransformer.copyAndTransform();
        Pair<Expr, List<Stmt>> rhsTransformResult = this.RHSTransformer.copyAndTransform();
        Expr copiedLHS = lhsTransformResult.getValue0();
        Expr copiedRHS = rhsTransformResult.getValue0();

        List<Stmt> newPrefixStatementList = new LinkedList<>();
        for (Stmt iter : lhsTransformResult.getValue1()) newPrefixStatementList.add(iter);
        for (Stmt iter : rhsTransformResult.getValue1()) newPrefixStatementList.add(iter);

        AndExpr copiedNode = (AndExpr) this.originalNode.copy();

        copiedLHS.setParent(copiedNode);
        copiedRHS.setParent(copiedNode);
        copiedNode.setLHS(copiedLHS);
        copiedNode.setRHS(copiedRHS);

        return new Pair<>(copiedNode, newPrefixStatementList);
    }
}
