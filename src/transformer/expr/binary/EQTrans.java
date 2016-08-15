package transformer.expr.binary;

import ast.*;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public final class EQTrans extends BinaryTrans{
    public EQTrans(ExprTransArgument argument, EQExpr eqExpr) {
        super(argument, eqExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !this.hasTransformOnCurrentNode();

        Pair<Expr, List<Stmt>> lhsTransformResult = this.LHSTransformer.copyAndTransform();
        Pair<Expr, List<Stmt>> rhsTransformResult = this.RHSTransformer.copyAndTransform();
        Expr copiedLHS = lhsTransformResult.getValue0();
        Expr copiedRHS = rhsTransformResult.getValue0();

        List<Stmt> newPrefixStatementList = new LinkedList<>();
        newPrefixStatementList.addAll(lhsTransformResult.getValue1());
        BiFunction<LValueExpr, Expr, AssignStmt> buildAssignStmt = (LValueExpr lhs, Expr rhs) -> {
            AssignStmt returnStmt = new AssignStmt();
            returnStmt.setLHS(lhs);
            returnStmt.setRHS(rhs);
            returnStmt.setOutputSuppressed(true);
            return returnStmt;
        };
        if (!rhsTransformResult.getValue1().isEmpty()) {
            String lhsOverrideName = this.alterNamespace.generateNewName();
            AssignStmt lhsOvrrideAssign = buildAssignStmt.apply(new NameExpr(new Name(lhsOverrideName)), copiedLHS);
            newPrefixStatementList.add(lhsOvrrideAssign);
            copiedLHS = new NameExpr(new Name(lhsOverrideName));
        }
        newPrefixStatementList.addAll(rhsTransformResult.getValue1());

        EQExpr copiedNode = (EQExpr) this.originalNode.copy();

        copiedLHS.setParent(copiedNode);
        copiedRHS.setParent(copiedNode);
        copiedNode.setLHS(copiedLHS);
        copiedNode.setRHS(copiedRHS);

        return new Pair<>(copiedNode, newPrefixStatementList);
    }
}