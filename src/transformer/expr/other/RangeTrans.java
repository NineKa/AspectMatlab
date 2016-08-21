package transformer.expr.other;

import ast.*;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public final class RangeTrans extends ExprTrans {
    private ExprTrans lowerTransformer = null;
    private ExprTrans increTransformer = null;
    private ExprTrans upperTransformer = null;

    public RangeTrans(TransformerArgument argument, RangeExpr rangeExpr) {
        super(argument, rangeExpr);
        this.lowerTransformer = ExprTrans.buildExprTransformer(argument, rangeExpr.getLower());
        this.upperTransformer = ExprTrans.buildExprTransformer(argument, rangeExpr.getUpper());
        if (rangeExpr.hasIncr()) {
            this.increTransformer = ExprTrans.buildExprTransformer(argument, rangeExpr.getIncr());
        }
    }

    public boolean hasIncrTransformer() {
        return this.increTransformer != null;
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !this.hasTransformOnCurrentNode();
        if (this.hasFurtherTransform()) {
            BiFunction<LValueExpr, Expr, AssignStmt> buildAssignStmt = (LValueExpr lhs, Expr rhs) -> {
                AssignStmt returnStmt = new AssignStmt();
                returnStmt.setLHS(lhs);
                returnStmt.setRHS(rhs);
                returnStmt.setOutputSuppressed(true);
                return returnStmt;
            };
            RangeExpr newRangeExpr = new RangeExpr();
            List<Stmt> newPrefixStatementList = new LinkedList<>();

            Pair<Expr, List<Stmt>> lowerTransformResult = this.lowerTransformer.copyAndTransform();
            Pair<Expr, List<Stmt>> upperTransformResult = this.upperTransformer.copyAndTransform();
            Pair<Expr, List<Stmt>> increTransformResult = (increTransformer == null)? null : increTransformer.copyAndTransform();

            String lowerAlterName = this.alterNamespace.generateNewName();
            String upperAlterName = this.alterNamespace.generateNewName();
            String increAlterName = this.alterNamespace.generateNewName();

            Expr copiedLower = lowerTransformResult.getValue0();
            Expr copiedUpper = upperTransformResult.getValue0();
            Expr copiedIncre = increTransformResult.getValue0();

            AssignStmt lowerAssign = buildAssignStmt.apply(new NameExpr(new Name(lowerAlterName)), copiedLower);
            AssignStmt upperAssign = buildAssignStmt.apply(new NameExpr(new Name(upperAlterName)), copiedUpper);
            AssignStmt increAssign = buildAssignStmt.apply(new NameExpr(new Name(increAlterName)), copiedIncre);

            newPrefixStatementList.addAll(lowerTransformResult.getValue1());
            newPrefixStatementList.add(lowerAssign);
            newPrefixStatementList.addAll(increTransformResult.getValue1());
            newPrefixStatementList.add(increAssign);
            newPrefixStatementList.addAll(upperTransformResult.getValue1());
            newPrefixStatementList.add(upperAssign);

            newRangeExpr.setLower(new NameExpr(new Name(lowerAlterName)));
            newRangeExpr.setIncr(new NameExpr(new Name(increAlterName)));
            newRangeExpr.setUpper(new NameExpr(new Name(upperAlterName)));

            return new Pair<>(newRangeExpr, newPrefixStatementList);
        } else {
            RangeExpr copiedNode = (RangeExpr) this.originalNode.treeCopy();
            return new Pair<>(copiedNode, new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        if (this.lowerTransformer.hasFurtherTransform()) return true;
        if (this.upperTransformer.hasFurtherTransform()) return true;
        if (this.hasIncrTransformer() && this.increTransformer.hasFurtherTransform()) return true;
        return false;
    }
}
