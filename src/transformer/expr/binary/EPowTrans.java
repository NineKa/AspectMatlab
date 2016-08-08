package transformer.expr.binary;

import ast.*;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public final class EPowTrans extends BinaryTrans {
    public EPowTrans(ExprTransArgument argument, EPowExpr ePowExpr) {
        super(argument, ePowExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        if (this.hasTransformOnCurrentNode()) {
            /* [expr1] .^ [expr2]   <=>     t0 = expr1          */
            /*                              t1 = expr2          */
            /*                              t2 = t0 .^ t1   *   */
            /*                              return t2           */
            BiFunction<LValueExpr, Expr, AssignStmt> buildAssignStmt = (LValueExpr lhs, Expr rhs) -> {
                AssignStmt returnStmt = new AssignStmt();
                returnStmt.setLHS(lhs);
                returnStmt.setRHS(rhs);
                returnStmt.setOutputSuppressed(true);
                return returnStmt;
            };
            Pair<Expr, List<Stmt>> lhsTransformResult = this.LHSTransformer.copyAndTransform();
            Pair<Expr, List<Stmt>> rhsTransformResult = this.RHSTransformer.copyAndTransform();
            Expr copiedLHS = lhsTransformResult.getValue0();
            Expr copiedRHS = rhsTransformResult.getValue0();

            List<Stmt> newPrefixStatementList = new LinkedList<>();
            for (Stmt iter : lhsTransformResult.getValue1()) newPrefixStatementList.add(iter);
            for (Stmt iter : rhsTransformResult.getValue1()) newPrefixStatementList.add(iter);

            String t0Name = this.alterNamespace.generateNewName();
            String t1Name = this.alterNamespace.generateNewName();
            String t2Name = this.alterNamespace.generateNewName();

            AssignStmt t0Assign = buildAssignStmt.apply(new NameExpr(new Name(t0Name)), copiedLHS);
            AssignStmt t1Assign = buildAssignStmt.apply(new NameExpr(new Name(t1Name)), copiedRHS);
            AssignStmt t2Assign = buildAssignStmt.apply(
                    new NameExpr(new Name(t2Name)),
                    new EPowExpr(new NameExpr(new Name(t0Name)), new NameExpr(new Name(t1Name)))
            );

            newPrefixStatementList.add(t0Assign);
            newPrefixStatementList.add(t1Assign);
            newPrefixStatementList.add(t2Assign);

            this.jointPointDelegate.accept(t2Assign);

            return new Pair<>(new NameExpr(new Name(t2Name)), newPrefixStatementList);
        } else {
            Pair<Expr, List<Stmt>> lhsTransformResult = this.LHSTransformer.copyAndTransform();
            Pair<Expr, List<Stmt>> rhsTransformResult = this.RHSTransformer.copyAndTransform();
            Expr copiedLHS = lhsTransformResult.getValue0();
            Expr copiedRHS = rhsTransformResult.getValue0();
            EPowExpr copiedNode = (EPowExpr) this.originalNode.copy();

            List<Stmt> newPrefixStatementList = new LinkedList<>();
            for (Stmt iter : lhsTransformResult.getValue1()) newPrefixStatementList.add(iter);
            for (Stmt iter : rhsTransformResult.getValue1()) newPrefixStatementList.add(iter);

            copiedLHS.setParent(copiedNode);
            copiedRHS.setParent(copiedNode);
            copiedNode.setLHS(copiedLHS);
            copiedNode.setRHS(copiedRHS);

            return new Pair<>(copiedNode, newPrefixStatementList);
        }
    }
}
