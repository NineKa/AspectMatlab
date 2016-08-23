package transformer.expr.other;

import ast.*;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

import java.util.LinkedList;
import java.util.List;

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
        assert originalNode instanceof RangeExpr;
        if (this.hasFurtherTransform()) {
            if (((RangeExpr) originalNode).hasIncr()) {
                List<Stmt> newPrefixStatementList = new LinkedList<>();

                Pair<Expr, List<Stmt>> lowerTransformResult = lowerTransformer.copyAndTransform();
                newPrefixStatementList.addAll(lowerTransformResult.getValue1());
                String lowerAlterName = alterNamespace.generateNewName();
                AssignStmt lowerAlterAssign = new AssignStmt();
                lowerAlterAssign.setLHS(new NameExpr(new Name(lowerAlterName)));
                lowerAlterAssign.setRHS(lowerTransformResult.getValue0());
                lowerAlterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(lowerAlterAssign);

                Pair<Expr, List<Stmt>> incrTransformResult = increTransformer.copyAndTransform();
                String incrAlterName = alterNamespace.generateNewName();
                AssignStmt incrAlterAssign = new AssignStmt();
                incrAlterAssign.setLHS(new NameExpr(new Name(incrAlterName)));
                incrAlterAssign.setRHS(incrTransformResult.getValue0());
                incrAlterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(incrAlterAssign);

                Pair<Expr, List<Stmt>> upperTransformResult = upperTransformer.copyAndTransform();
                newPrefixStatementList.addAll(upperTransformResult.getValue1());
                String upperAlterName = alterNamespace.generateNewName();
                AssignStmt upperAlterassign = new AssignStmt();
                upperAlterassign.setLHS(new NameExpr(new Name(upperAlterName)));
                upperAlterassign.setRHS(upperTransformResult.getValue0());
                upperAlterassign.setOutputSuppressed(true);
                newPrefixStatementList.add(upperAlterassign);

                RangeExpr retExpr = new RangeExpr();
                retExpr.setLower(new NameExpr(new Name(lowerAlterName)));
                retExpr.setIncr(new NameExpr(new Name(incrAlterName)));
                retExpr.setUpper(new NameExpr(new Name(upperAlterName)));

                return new Pair<>(retExpr, newPrefixStatementList);
            } else {
                List<Stmt> newPrefixStatementList = new LinkedList<>();

                Pair<Expr, List<Stmt>> lowerTransformResult = lowerTransformer.copyAndTransform();
                newPrefixStatementList.addAll(lowerTransformResult.getValue1());
                String lowerAlterName = alterNamespace.generateNewName();
                AssignStmt lowerAlterAssign = new AssignStmt();
                lowerAlterAssign.setLHS(new NameExpr(new Name(lowerAlterName)));
                lowerAlterAssign.setRHS(lowerTransformResult.getValue0());
                lowerAlterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(lowerAlterAssign);

                Pair<Expr, List<Stmt>> upperTransformResult = upperTransformer.copyAndTransform();
                newPrefixStatementList.addAll(upperTransformResult.getValue1());
                String upperAlterName = alterNamespace.generateNewName();
                AssignStmt upperAlterAssign = new AssignStmt();
                upperAlterAssign.setLHS(new NameExpr(new Name(upperAlterName)));
                upperAlterAssign.setRHS(upperTransformResult.getValue0());
                upperAlterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(upperAlterAssign);

                RangeExpr retExpr = new RangeExpr();
                retExpr.setLower(new NameExpr(new Name(lowerAlterName)));
                retExpr.setUpper(new NameExpr(new Name(upperAlterName)));

                return new Pair<>(retExpr, newPrefixStatementList);
            }
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
