package transformer.expr.lvalue;

import ast.*;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;
import transformer.jointpoint.AMJointPointGet;
import transformer.jointpoint.AMJointPointSet;
import transformer.util.AccessMode;

import java.util.LinkedList;
import java.util.List;

public final class DotTrans extends LValueTrans {
    private ExprTrans targetTransformer = null;

    public DotTrans(TransformerArgument argument, DotExpr dotExpr) {
        super(argument, dotExpr);

        this.targetTransformer = ExprTrans.buildExprTransformer(argument, dotExpr.getTarget());
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        if (runtimeInfo.accessMode == AccessMode.Read) {
            return accessModeReadHandle();
        } else {
            assert runtimeInfo.accessMode == AccessMode.Write;
            return accessModeWriteHandle();
        }
    }

    @SuppressWarnings("deprecation")
    private Pair<Expr, List<Stmt>> accessModeWriteHandle() {
        assert runtimeInfo.accessMode == AccessMode.Write;
        assert originalNode instanceof DotExpr;
        if (hasTransformOnCurrentNode()) {
            DotExpr retExpr = new DotExpr();
            List<Stmt> newPrefixStatementList = new LinkedList<>();

            assert ((DotExpr) originalNode).getTarget() instanceof NameExpr;
            String targetName = ((NameExpr) ((DotExpr) originalNode).getTarget()).getName().getID();
            String fieldName = ((DotExpr) originalNode).getField().getID();

            /* [expr1].[expr2]  <=>     if exist('[expr1]','var')                   */
            /*                              t0 = [expr1];                           */
            /*                          end                                         */
            /*                          return t0.[expr2]                       =   */
            /*                          [[expr1].[expr2]] = deal(t0.[expr2])    *   */

            IfBlock existingCheckBlock = new IfBlock();
            existingCheckBlock.setCondition(new ParameterizedExpr(
                    new NameExpr(new Name("exist")),
                    new ast.List<Expr>(
                            new StringLiteralExpr(targetName),
                            new StringLiteralExpr("var")
                    )
            ));

            String t0Name = alterNamespace.generateNewName();
            AssignStmt t0Assign = new AssignStmt();
            t0Assign.setLHS(new NameExpr(new Name(t0Name)));
            t0Assign.setRHS(new NameExpr(new Name(targetName)));
            t0Assign.setOutputSuppressed(true);
            existingCheckBlock.addStmt(t0Assign);
            newPrefixStatementList.add(new IfStmt(new ast.List<IfBlock>(existingCheckBlock), new Opt<ElseBlock>()));

            retExpr.setTarget(new NameExpr(new Name(t0Name)));
            retExpr.setField(new Name(t0Name));

            AssignStmt t0RetrieveAssign = new AssignStmt();
            t0RetrieveAssign.setLHS(new MatrixExpr(new ast.List<Row>(new Row(new ast.List<Expr>(new DotExpr(
                    new NameExpr(new Name(targetName)), new Name(fieldName)
            ))))));
            t0RetrieveAssign.setRHS(new ParameterizedExpr(
                    new NameExpr(new Name("deal")),
                    new ast.List<Expr>(new DotExpr(new NameExpr(new Name(t0Name)), new Name(fieldName)))
            ));
            t0RetrieveAssign.setOutputSuppressed(true);
            assignRetriveStack.push(t0RetrieveAssign);

            /* invoke joint point delegate */
            AMJointPointSet jointPoint = new AMJointPointSet(
                    t0RetrieveAssign, originalNode.getStartLine(),
                    originalNode.getStartColumn(), enclosingFilename
            );
            jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
            jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                    new StringLiteralExpr(fieldName)
            )))));
            jointPoint.setNewVarExpr(new NameExpr(new Name(t0Name)));
            jointPoint.setOldVarExpr(new NameExpr(new Name(targetName)));
            jointPointDelegate.accept(jointPoint);

            return new Pair<>(retExpr, newPrefixStatementList);
        } else {
            if (!hasFurtherTransform()) {
                Expr copiedExpr = originalNode.treeCopy();
                return new Pair<>(copiedExpr, new LinkedList<>());
            }
            DotExpr retExpr = new DotExpr();
            List<Stmt> newPrefixStatementList = new LinkedList<>();

            Expr targetExpr;
            if (((DotExpr) originalNode).getTarget() instanceof NameExpr) {
                targetExpr = (NameExpr) ((DotExpr) originalNode).getTarget().treeCopy();
            } else {
                Pair<Expr, List<Stmt>> targetTransformResult = targetTransformer.copyAndTransform();
                newPrefixStatementList.addAll(targetTransformResult.getValue1());

                targetExpr =targetTransformResult.getValue0();
            }

            retExpr.setTarget(targetExpr);
            retExpr.setField(((DotExpr) originalNode).getField().treeCopy());

            return new Pair<>(retExpr, newPrefixStatementList);
        }
    }

    private Pair<Expr, List<Stmt>> accessModeReadHandle() {
        assert runtimeInfo.accessMode == AccessMode.Read;
        assert originalNode instanceof DotExpr;
        if (hasTransformOnCurrentNode()) {
            /*  [expr1].[expr2]     <=>     t0 = {[expr1].[expr2]};     *   */
            /*                              return t0{:}                    */
            assert ((DotExpr) originalNode).getTarget() instanceof NameExpr;
            CellIndexExpr retExpr = new CellIndexExpr();
            List<Stmt> newPrefixStatementList = new LinkedList<>();

            String targetName = ((NameExpr) ((DotExpr) originalNode).getTarget()).getName().getID();
            String fieldName = ((DotExpr) originalNode).getField().getID();

            String t0Name = alterNamespace.generateNewName();
            AssignStmt t0Assign = new AssignStmt();
            t0Assign.setLHS(new NameExpr(new Name(t0Name)));
            t0Assign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                    new DotExpr(new NameExpr(new Name(targetName)), new Name(fieldName))
            )))));
            t0Assign.setOutputSuppressed(true);
            newPrefixStatementList.add(t0Assign);

            /* invoke joint point delegate */
            AMJointPointGet jointPoint = new AMJointPointGet(
                    t0Assign, originalNode.getStartLine(),
                    originalNode.getStartColumn(), enclosingFilename
            );
            jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
            jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                    new StringLiteralExpr(fieldName)
            )))));
            jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
            jointPointDelegate.accept(jointPoint);

            retExpr.setTarget(new NameExpr(new Name(t0Name)));
            retExpr.addArg(new ColonExpr());

            return new Pair<>(retExpr, newPrefixStatementList);
        } else {
            if (!hasFurtherTransform()) {
                Expr copiedExpr = originalNode.treeCopy();
                return new Pair<>(copiedExpr, new LinkedList<>());
            }
            DotExpr retExpr = new DotExpr();
            List<Stmt> newPrefixStatementList = new LinkedList<>();

            Pair<Expr, List<Stmt>> targetTransformResult = targetTransformer.copyAndTransform();
            newPrefixStatementList.addAll(targetTransformResult.getValue1());

            retExpr.setTarget(targetTransformResult.getValue0());
            retExpr.setField(((DotExpr) originalNode).getField().treeCopy());

            return new Pair<>(retExpr, newPrefixStatementList);
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        assert originalNode instanceof DotExpr;
        if (((DotExpr) originalNode).getTarget() instanceof NameExpr) {
            if (hasTransformOnCurrentNode()) return true;
            return false;
        } else {
            assert !hasTransformOnCurrentNode();
            if (targetTransformer.hasFurtherTransform()) return true;
            return false;
        }
    }
}
