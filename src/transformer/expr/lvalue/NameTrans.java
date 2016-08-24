package transformer.expr.lvalue;

import abstractPattern.Action;
import ast.*;
import natlab.DecIntNumericLiteralValue;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import natlab.toolkits.analysis.varorfun.VFDatum;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.jointpoint.AMJointPointCall;
import transformer.jointpoint.AMJointPointGet;
import transformer.jointpoint.AMJointPointSet;
import transformer.util.AccessMode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class NameTrans extends LValueTrans {
    private String targetName = null;
    private VFDatum kindAnalysisResult = null;

    public NameTrans(TransformerArgument argument, NameExpr nameExpr) {
        super(argument, nameExpr);
        targetName = nameExpr.getName().getID();
        VFAnalysis kindAnalysis = runtimeInfo.kindAnalysis;
        try {
            kindAnalysisResult = kindAnalysis.getResult(nameExpr.getName());
        } catch (NullPointerException exception) {
            throw new AssertionError();         /* report bug in kind analysis */
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        if (hasTransformOnCurrentNode()) {
            if (runtimeInfo.accessMode == AccessMode.Write) {
                assert kindAnalysisResult.isVariable();
                /* [expr]   <=>     if exist('[expr]','var')        */
                /*                      t0 = [expr]                 */
                /*                  end                             */
                /*                  return t0                       */
                List<Stmt> newPrefixStatementList = new LinkedList<>();

                IfBlock existingAssignBlock = new IfBlock();
                existingAssignBlock.setCondition(new ParameterizedExpr(
                        new NameExpr(new Name("exist")),
                        new ast.List<Expr>(new StringLiteralExpr(targetName), new StringLiteralExpr("var"))
                ));
                String t0Name = alterNamespace.generateNewName();
                AssignStmt t0Assign = new AssignStmt();
                t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                t0Assign.setRHS(new NameExpr(new Name(targetName)));
                t0Assign.setOutputSuppressed(true);
                existingAssignBlock.addStmt(t0Assign);

                IfStmt prefixIfStmt = new IfStmt(new ast.List<>(existingAssignBlock), new Opt<>());
                newPrefixStatementList.add(prefixIfStmt);

                AssignStmt t0RetrieveAssign = new AssignStmt();
                t0RetrieveAssign.setLHS(new NameExpr(new Name(targetName)));
                t0RetrieveAssign.setRHS(new NameExpr(new Name(t0Name)));
                t0RetrieveAssign.setOutputSuppressed(true);
                assignRetriveStack.push(t0RetrieveAssign);

                /* invoke joint point delegate */
                AMJointPointSet jointPoint = new AMJointPointSet(
                        t0RetrieveAssign, originalNode.getStartLine(),
                        originalNode.getStartColumn(), enclosingFilename
                );
                jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
                jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>()))));
                jointPoint.setOldVarExpr(new NameExpr(new Name(targetName)));
                jointPoint.setNewVarExpr(new NameExpr(new Name(t0Name)));
                jointPointDelegate.accept(jointPoint);

                return new Pair<>(new NameExpr(new Name(t0Name)), newPrefixStatementList);
            }
            assert runtimeInfo.accessMode == AccessMode.Read;
            if (kindAnalysisResult.isFunction()) {
                /* [expr]   <=>     t0 = {}                     */
                /*                  t1 = [expr](t0{:})      *   */
                /*                  return t1                   */
                List<Stmt> newPrefixStatementList = new LinkedList<>();

                String t0Name = alterNamespace.generateNewName();
                AssignStmt t0Assign = new AssignStmt();
                t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                t0Assign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>()))));
                t0Assign.setOutputSuppressed(true);
                newPrefixStatementList.add(t0Assign);

                String t1Name = alterNamespace.generateNewName();
                AssignStmt t1Assign = new AssignStmt();
                t1Assign.setLHS(new NameExpr(new Name(t1Name)));
                t1Assign.setRHS(new ParameterizedExpr(
                        new NameExpr(new Name(targetName)),
                        new ast.List<>(new CellIndexExpr(
                                new NameExpr(new Name(t0Name)),
                                new ast.List<>(new ColonExpr())
                        ))
                ));
                t1Assign.setOutputSuppressed(true);
                newPrefixStatementList.add(t1Assign);

                /* invoke joint point call back function */
                AMJointPointCall jointPoint = new AMJointPointCall(
                        t1Assign, originalNode.getStartLine(),
                        originalNode.getStartColumn(), enclosingFilename
                );
                jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
                jointPoint.setArgumentExpr(new NameExpr(new Name(t0Name)));
                jointPoint.setFunctionHandleExpr(new FunctionHandleExpr(new Name(targetName)));
                jointPointDelegate.accept(jointPoint);

                return new Pair<>(new NameExpr(new Name(t1Name)), newPrefixStatementList);
            }
            if (kindAnalysisResult.isID()) {
                /* [expr]   <=>     if exist('[expr]') ~= 1     */
                /*                      t0 = {}                 */
                /*                      t1 = [expr](t0{:})  *   */
                /*                  else                        */
                /*                      t1 = [expr]         *   */
                /*                  end                         */
                /*                  return t1                   */
                String t0Name = alterNamespace.generateNewName();
                String t1Name = alterNamespace.generateNewName();
                List<Stmt> newPrefixStatementList = new LinkedList<>();

                IfBlock subroutineCallBlock = new IfBlock();
                subroutineCallBlock.setCondition(new NEExpr(
                        new ParameterizedExpr(
                                new NameExpr(new Name("exist")),
                                new ast.List<Expr>(new StringLiteralExpr(targetName))
                        ),
                        new IntLiteralExpr(new DecIntNumericLiteralValue("1"))
                ));
                AssignStmt t0AssignFunc = new AssignStmt();
                t0AssignFunc.setLHS(new NameExpr(new Name(t0Name)));
                t0AssignFunc.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>()))));
                t0AssignFunc.setOutputSuppressed(true);
                subroutineCallBlock.addStmt(t0AssignFunc);

                AssignStmt t1AssignFunc = new AssignStmt();
                t1AssignFunc.setLHS(new NameExpr(new Name(t1Name)));
                t1AssignFunc.setRHS(new ParameterizedExpr(
                        new NameExpr(new Name(targetName)),
                        new ast.List<Expr>(new CellIndexExpr(
                                new NameExpr(new Name(t0Name)),
                                new ast.List<Expr>(new ColonExpr())
                        ))
                ));
                t1AssignFunc.setOutputSuppressed(true);
                subroutineCallBlock.addStmt(t1AssignFunc);

                ElseBlock variableGetBlock = new ElseBlock();
                AssignStmt t1AssignVar = new AssignStmt();
                t1AssignVar.setLHS(new NameExpr(new Name(t1Name)));
                t1AssignVar.setRHS(new NameExpr(new Name(targetName)));
                t1AssignVar.setOutputSuppressed(true);
                variableGetBlock.addStmt(t1AssignVar);

                IfStmt prefixIfStmt = new IfStmt(new ast.List<>(subroutineCallBlock), new Opt<>(variableGetBlock));
                newPrefixStatementList.add(prefixIfStmt);

                /* invoke joint point call back function */
                assert originalNode instanceof NameExpr;
                runtimeInfo.kindAnalysis.override(((NameExpr) originalNode).getName(), VFDatum.FUN);
                Collection<Action> actionsAsFunction = getPossibleAttachedActionsSet();
                runtimeInfo.kindAnalysis.override(((NameExpr) originalNode).getName(), VFDatum.VAR);
                Collection<Action> actionsAsVariable = getPossibleAttachedActionsSet();
                runtimeInfo.kindAnalysis.removeOverride(((NameExpr) originalNode).getName());
                if (!actionsAsFunction.isEmpty()) { /* invoke as a function call */
                    AMJointPointCall jointPoint  = new AMJointPointCall(
                            t1AssignFunc, originalNode.getStartLine(),
                            originalNode.getStartColumn(), enclosingFilename
                    );
                    jointPoint.addAllMatchedAction(actionsAsFunction);
                    jointPoint.setArgumentExpr(new NameExpr(new Name(t0Name)));
                    jointPoint.setFunctionHandleExpr(new FunctionHandleExpr(new Name(t1Name)));
                    jointPointDelegate.accept(jointPoint);
                }
                if (!actionsAsVariable.isEmpty()) { /* invoke as a variable get  */
                    AMJointPointGet jointPoint = new AMJointPointGet(
                            t1AssignVar, originalNode.getStartLine(),
                            originalNode.getStartColumn(), enclosingFilename
                    );
                    jointPoint.addAllMatchedAction(actionsAsVariable);
                    jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>()))));
                    jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
                    jointPointDelegate.accept(jointPoint);
                }

                return new Pair<>(new NameExpr(new Name(t1Name)), newPrefixStatementList);
            }
            if (kindAnalysisResult.isVariable()) {
                /*                  t0 = [expr]             *   */
                /*                  return t0                   */
                List<Stmt> newPrefixStatementList = new LinkedList<>();
                String t0Name = alterNamespace.generateNewName();
                AssignStmt t0Assign = new AssignStmt();
                t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                t0Assign.setRHS(new NameExpr(new Name(targetName)));
                t0Assign.setOutputSuppressed(true);
                newPrefixStatementList.add(t0Assign);

                /* invoke joint point call back function */
                AMJointPointGet jointPoint = new AMJointPointGet(
                        t0Assign, originalNode.getStartLine(),
                        originalNode.getStartColumn(), enclosingFilename
                );
                jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
                jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>()))));
                jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
                jointPointDelegate.accept(jointPoint);

                return new Pair<>(new NameExpr(new Name(t0Name)), newPrefixStatementList);
            }
            /* control flow should not reach here */
            throw new AssertionError();
        } else {
            Expr copiedExpr = new NameExpr(new Name(targetName));
            return new Pair<>(copiedExpr, new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        if (hasTransformOnCurrentNode()) return true;
        return false;
    }
}
