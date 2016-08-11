package transformer.expr.binary;

import ast.Expr;
import ast.OrExpr;
import ast.Stmt;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;

public final class OrTrans extends BinaryTrans {
    public OrTrans(ExprTransArgument argument, OrExpr orExpr) {
        super(argument, orExpr);
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

        OrExpr copiedNode = (OrExpr) this.originalNode.copy();

        copiedLHS.setParent(copiedNode);
        copiedRHS.setParent(copiedNode);
        copiedNode.setLHS(copiedLHS);
        copiedNode.setRHS(copiedRHS);

        return new Pair<>(copiedNode, newPrefixStatementList);
    }
}
