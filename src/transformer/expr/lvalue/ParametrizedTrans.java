package transformer.expr.lvalue;

import abstractPattern.Action;
import ast.*;
import natlab.DecIntNumericLiteralValue;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import natlab.toolkits.analysis.varorfun.VFDatum;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;
import transformer.jointpoint.AMJointPointCall;
import transformer.jointpoint.AMJointPointGet;
import transformer.jointpoint.AMJointPointSet;
import transformer.util.AccessMode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ParametrizedTrans extends LValueTrans {
    private ExprTrans targetTransformer = null;
    private List<ExprTrans> argumentTransformerList = new LinkedList<>();

    public ParametrizedTrans(TransformerArgument argument, ParameterizedExpr parameterizedExpr) {
        super(argument, parameterizedExpr);
        targetTransformer = ExprTrans.buildExprTransformer(argument, parameterizedExpr.getTarget());

        TransformerArgument argArgument = argument.copy();
        argArgument.runtimeInfo.accessMode = AccessMode.Read;

        for (Expr arg : parameterizedExpr.getArgList()) {
            ExprTrans appendingTrans = ExprTrans.buildExprTransformer(argArgument, arg);
            argumentTransformerList.add(appendingTrans);
        }
    }

    private boolean hasFurtherTransformArgument() {
        for (ExprTrans exprTrans : argumentTransformerList) {
            if (exprTrans.hasFurtherTransform()) return true;
        }
        return false;
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        if (runtimeInfo.accessMode == AccessMode.Read) {
            return accessModeReadHandle();
        } else {
            return accessModeWriteHandle();
        }
    }

    public boolean hasFurtherTransform() {
        assert originalNode instanceof ParameterizedExpr;
        if (((ParameterizedExpr) originalNode).getTarget() instanceof NameExpr) {
            if (hasTransformOnCurrentNode()) return true;
            if (hasFurtherTransformArgument()) return true;
            return false;
        } else {
            if (targetTransformer.hasFurtherTransform()) return true;
            if (hasFurtherTransformArgument()) return true;
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private Pair<Expr, List<Stmt>> accessModeWriteHandle() {
        assert runtimeInfo.accessMode == AccessMode.Write;
        assert originalNode instanceof ParameterizedExpr;
        ParameterizedExpr retExpr = new ParameterizedExpr();
        List<Stmt> newPrefixStatementList = new LinkedList<>();
        if (hasTransformOnCurrentNode()) {
            assert ((ParameterizedExpr) originalNode).getTarget() instanceof NameExpr;
            /* [expr1]([expr2])   <=>   if exist('[expr1]','var')           */
            /*                             t0 = [expr1];                    */
            /*                          end                                 */
            /*                          [expr2(transform)]                  */
            /*                          return t0([expr2])              =   */
            /*                          [expr1]([expr2]) = t0([expr2])  *   */

            String targetName = ((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName().getID();

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

            resolveEndExpr(new NameExpr(new Name(targetName)));

            Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArgument();
            newPrefixStatementList.addAll(argumentTransformResult.getValue1());
            Pair<ast.List<Expr>, List<Stmt>> colonResolvedArg = resolveColonExpr(
                    argumentTransformResult.getValue0(),
                    new NameExpr(new Name(targetName))
            );
            newPrefixStatementList.addAll(colonResolvedArg.getValue1());

            ast.List<Expr> newArgList = colonResolvedArg.getValue0();

            retExpr.setTarget(new NameExpr(new Name(t0Name)));
            retExpr.setArgList(newArgList);

            AssignStmt t0RetrieveAssign = new AssignStmt();
            t0RetrieveAssign.setLHS(new ParameterizedExpr(new NameExpr(new Name(targetName)), newArgList.treeCopy()));
            t0RetrieveAssign.setRHS(new ParameterizedExpr(new NameExpr(new Name(t0Name)), newArgList.treeCopy()));
            t0RetrieveAssign.setOutputSuppressed(true);
            assignRetriveStack.push(t0RetrieveAssign);

            /* invoke joint point delegate */
            AMJointPointSet jointPoint = new AMJointPointSet(
                    t0RetrieveAssign, originalNode.getStartLine(),
                    originalNode.getStartColumn(), enclosingFilename
            );
            jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
            jointPoint.setNewVarExpr(new NameExpr(new Name(t0Name)));
            jointPoint.setOldVarExpr(new NameExpr(new Name(targetName)));
            jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(newArgList.treeCopy()))));
            jointPointDelegate.accept(jointPoint);

            return new Pair<>(retExpr, newPrefixStatementList);
        } else {
            if (!hasFurtherTransform()) {
                Expr copiedExpr = originalNode.treeCopy();
                return new Pair<>(copiedExpr, new LinkedList<>());
            }
            if (hasEndExpression()) {
                NameExpr targetExpr;
                if (((ParameterizedExpr) originalNode).getTarget() instanceof NameExpr) {
                    targetExpr = (NameExpr) ((ParameterizedExpr) originalNode).getTarget().treeCopy();
                } else {
                    /* [expr1]{[expr2]}     <=>     [expr1(transform)]          */
                    /*                              t0 = [expr1];               */
                    /*                              targetExpr = t0;            */
                    Pair<Expr, List<Stmt>> targetTransformResult = targetTransformer.copyAndTransform();
                    newPrefixStatementList.addAll(targetTransformResult.getValue1());

                    String t0Name = alterNamespace.generateNewName();
                    AssignStmt t0Assign = new AssignStmt();
                    t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                    t0Assign.setRHS(targetTransformResult.getValue0().treeCopy());
                    t0Assign.setOutputSuppressed(true);
                    newPrefixStatementList.add(t0Assign);

                    targetExpr = new NameExpr(new Name(t0Name));

                    AssignStmt t0RetrieveAssign = new AssignStmt();
                    t0RetrieveAssign.setLHS(targetTransformResult.getValue0().treeCopy());
                    t0RetrieveAssign.setRHS(new NameExpr(new Name(t0Name)));
                    t0RetrieveAssign.setOutputSuppressed(true);
                    assignRetriveStack.push(t0RetrieveAssign);
                }
                resolveEndExpr(targetExpr);

                Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArgument();
                newPrefixStatementList.addAll(argumentTransformResult.getValue1());

                retExpr.setTarget(targetExpr);
                retExpr.setArgList(argumentTransformResult.getValue0());

                return new Pair<>(retExpr, newPrefixStatementList);
            } else {
                Expr targetExp;
                if (((ParameterizedExpr) originalNode).getTarget() instanceof NameExpr) {
                    targetExp = ((ParameterizedExpr) originalNode).getTarget().treeCopy();
                } else {
                    Pair<Expr, List<Stmt>> targetTransformResult = targetTransformer.copyAndTransform();
                    newPrefixStatementList.addAll(targetTransformResult.getValue1());
                    targetExp = targetTransformResult.getValue0();
                }


                Pair<ast.List<Expr>, List<Stmt>> argumentListTransformResult = copyAndTransformArgument();
                newPrefixStatementList.addAll(argumentListTransformResult.getValue1());

                retExpr.setTarget(targetExp);
                retExpr.setArgList(argumentListTransformResult.getValue0());

                return new Pair<>(retExpr, newPrefixStatementList);
            }
        }
    }

    private Pair<Expr, List<Stmt>> accessModeReadHandle() {
        assert runtimeInfo.accessMode == AccessMode.Read;
        assert originalNode instanceof ParameterizedExpr;
        if (hasTransformOnCurrentNode()) {
            assert ((ParameterizedExpr) originalNode).getTarget() instanceof NameExpr;
            VFAnalysis kindAnalysis = runtimeInfo.kindAnalysis;
            VFDatum kindAnalysisResult = kindAnalysis.getResult(
                    ((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName()
            );

            String targetName = ((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName().getID();
            Expr retExpr = null;
            List<Stmt> newPrefixStatementList = new LinkedList<>();
            if (hasEndExpression() || hasColonExpression()) {
                assert kindAnalysisResult.isVariable() || kindAnalysisResult.isID();
                resolveEndExpr(new NameExpr(new Name(targetName)));

                Pair<ast.List<Expr>, List<Stmt>> transformedArgument = copyAndTransformArgument();
                newPrefixStatementList.addAll(transformedArgument.getValue1());
                Pair<ast.List<Expr>, List<Stmt>> colonResolvedArg = resolveColonExpr(
                        transformedArgument.getValue0(),
                        new NameExpr(new Name(targetName))
                );
                newPrefixStatementList.addAll(colonResolvedArg.getValue1());

                ast.List<Expr> newArgumentList = colonResolvedArg.getValue0();

                String alterName = alterNamespace.generateNewName();
                AssignStmt alterAssign = new AssignStmt();
                alterAssign.setLHS(new NameExpr(new Name(alterName)));
                alterAssign.setRHS(new ParameterizedExpr(new NameExpr(new Name(targetName)), newArgumentList));
                alterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(alterAssign);

                /* invoke joint point delegate */
                runtimeInfo.kindAnalysis.override(
                        ((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName(),
                        VFDatum.VAR
                );
                Collection<abstractPattern.Action> possibleActionSet = getPossibleAttachedActionsSet();
                if (!possibleActionSet.isEmpty()) {
                    AMJointPointGet jointPoint = new AMJointPointGet(
                            alterAssign, originalNode.getStartLine(),
                            originalNode.getStartColumn(), enclosingFilename
                    );
                    jointPoint.addAllMatchedAction(possibleActionSet);
                    jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
                    jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(newArgumentList.treeCopy()))));

                    jointPointDelegate.accept(jointPoint);
                }
                runtimeInfo.kindAnalysis.removeOverride(
                        ((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName()
                );

                retExpr = new NameExpr(new Name(alterName));
                return new Pair<>(retExpr, newPrefixStatementList);
            } else {
                if (kindAnalysisResult.isVariable()) {
                    Pair<ast.List<Expr>, List<Stmt>> transformedArgument = copyAndTransformArgument();
                    newPrefixStatementList.addAll(transformedArgument.getValue1());

                    String alterName = alterNamespace.generateNewName();
                    AssignStmt alterAssign = new AssignStmt();
                    alterAssign.setLHS(new NameExpr(new Name(alterName)));
                    alterAssign.setRHS(new ParameterizedExpr(
                            new NameExpr(new Name(targetName)),
                            transformedArgument.getValue0()
                    ));
                    alterAssign.setOutputSuppressed(true);
                    newPrefixStatementList.add(alterAssign);

                    /* invoke joint point delegate */
                    AMJointPointGet jointPoint = new AMJointPointGet(
                            alterAssign, originalNode.getStartLine(),
                            originalNode.getStartColumn(), enclosingFilename
                    );
                    jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
                    jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(transformedArgument.getValue0()))));
                    jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
                    jointPointDelegate.accept(jointPoint);

                    retExpr = new NameExpr(new Name(alterName));
                    return new Pair<>(retExpr, newPrefixStatementList);
                }
                if (kindAnalysisResult.isFunction()) {
                    Pair<ast.List<Expr>, List<Stmt>> transformedArgument = copyAndTransformArgument();
                    newPrefixStatementList.addAll(transformedArgument.getValue1());

                    String argCellName = alterNamespace.generateNewName();
                    AssignStmt argCellAssign = new AssignStmt();
                    argCellAssign.setLHS(new NameExpr(new Name(argCellName)));
                    argCellAssign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(transformedArgument.getValue0()))));
                    argCellAssign.setOutputSuppressed(true);
                    newPrefixStatementList.add(argCellAssign);

                    String alterName = alterNamespace.generateNewName();
                    AssignStmt alterAssign = new AssignStmt();
                    alterAssign.setLHS(new NameExpr(new Name(alterName)));
                    alterAssign.setRHS(new ParameterizedExpr(
                            new NameExpr(new Name(targetName)),
                            new ast.List<Expr>(new CellIndexExpr(
                                    new NameExpr(new Name(argCellName)),
                                    new ast.List<Expr>(new ColonExpr()))
                            )
                    ));
                    alterAssign.setOutputSuppressed(true);
                    newPrefixStatementList.add(alterAssign);

                    /* invoke joint point delegate */
                    AMJointPointCall jointPoint = new AMJointPointCall(
                            alterAssign, originalNode.getStartLine(),
                            originalNode.getStartColumn(), enclosingFilename
                    );
                    jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
                    jointPoint.setArgumentExpr(new NameExpr(new Name(argCellName)));
                    jointPoint.setFunctionHandleExpr(new FunctionHandleExpr(new Name(targetName)));
                    jointPointDelegate.accept(jointPoint);

                    retExpr = new NameExpr(new Name(alterName));
                    return new Pair<>(retExpr, newPrefixStatementList);
                }
                if (kindAnalysisResult.isID()) {
                    Pair<ast.List<Expr>, List<Stmt>> transformedArgument = copyAndTransformArgument();
                    newPrefixStatementList.addAll(transformedArgument.getValue1());

                    /* subroutine call block */
                    IfBlock subroutineBlock = new IfBlock();
                    subroutineBlock.setCondition(new NEExpr(
                            new ParameterizedExpr(
                                    new NameExpr(new Name("exist")),
                                    new ast.List<Expr>(new StringLiteralExpr(targetName))
                            ),
                            new IntLiteralExpr(new DecIntNumericLiteralValue("1"))
                    ));

                    String argCellName = alterNamespace.generateNewName();
                    AssignStmt argCellAssign = new AssignStmt();
                    argCellAssign.setLHS(new NameExpr(new Name(argCellName)));
                    argCellAssign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(transformedArgument.getValue0().treeCopy()))));
                    argCellAssign.setOutputSuppressed(true);
                    subroutineBlock.addStmt(argCellAssign);

                    String alterName = alterNamespace.generateNewName();
                    AssignStmt funcAlterAssign = new AssignStmt();
                    funcAlterAssign.setLHS(new NameExpr(new Name(alterName)));
                    funcAlterAssign.setRHS(new ParameterizedExpr(
                            new NameExpr(new Name(targetName)),
                            new ast.List<Expr>(new NameExpr(new Name(argCellName)))
                    ));
                    funcAlterAssign.setOutputSuppressed(true);
                    subroutineBlock.addStmt(funcAlterAssign);

                    /* variable access block */
                    ElseBlock variableAccessBlock = new ElseBlock();

                    AssignStmt varAlterAssign = new AssignStmt();
                    varAlterAssign.setLHS(new NameExpr(new Name(alterName)));
                    varAlterAssign.setRHS(new ParameterizedExpr(
                            new NameExpr(new Name(targetName)),
                            transformedArgument.getValue0().treeCopy()
                    ));
                    varAlterAssign.setOutputSuppressed(true);
                    variableAccessBlock.addStmt(varAlterAssign);

                    IfStmt appendingIf = new IfStmt(new ast.List<>(subroutineBlock), new Opt<>(variableAccessBlock));
                    newPrefixStatementList.add(appendingIf);

                    /* invoke joint point delegate */
                    runtimeInfo.kindAnalysis.override(
                            ((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName(),
                            VFDatum.VAR
                    );
                    Collection<Action> variableSet = getPossibleAttachedActionsSet();
                    runtimeInfo.kindAnalysis.override(
                            ((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName(),
                            VFDatum.FUN
                    );
                    Collection<Action> subroutineSet = getPossibleAttachedActionsSet();
                    runtimeInfo.kindAnalysis.removeOverride(((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName());

                    if (!variableSet.isEmpty()) {
                        AMJointPointGet jointPoint = new AMJointPointGet(
                                varAlterAssign, originalNode.getStartLine(),
                                originalNode.getStartColumn(), enclosingFilename
                        );
                        jointPoint.addAllMatchedAction(variableSet);
                        jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(transformedArgument.getValue0()))));
                        jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
                        jointPointDelegate.accept(jointPoint);
                    }
                    if (!subroutineSet.isEmpty()) {
                        AMJointPointCall jointPoint = new AMJointPointCall(
                                varAlterAssign, originalNode.getStartLine(),
                                originalNode.getStartColumn(), enclosingFilename
                        );
                        jointPoint.addAllMatchedAction(subroutineSet);
                        jointPoint.setFunctionHandleExpr(new FunctionHandleExpr(new Name(targetName)));
                        jointPoint.setArgumentExpr(new NameExpr(new Name(argCellName)));
                        jointPointDelegate.accept(jointPoint);
                    }
                    assert !variableSet.isEmpty() || !subroutineSet.isEmpty();

                    retExpr = new NameExpr(new Name(alterName));
                    return new Pair<>(retExpr, newPrefixStatementList);
                }
                /* control flow should not reach here */
                throw new AssertionError();
            }
        } else {
            if (!hasFurtherTransform()) {
                Expr copiedExpr = originalNode.treeCopy();
                return new Pair<>(copiedExpr, new LinkedList<>());
            }

            ParameterizedExpr retExpr = new ParameterizedExpr();
            List<Stmt> newPrefixStatementList = new LinkedList<>();

            NameExpr targetExpr;
            if (((ParameterizedExpr) originalNode).getTarget() instanceof NameExpr) {
                targetExpr = new NameExpr(new Name(
                        ((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName().getID()
                ));
            } else {
                Pair<Expr, List<Stmt>> targetTransformResult = targetTransformer.copyAndTransform();
                newPrefixStatementList.addAll(targetTransformResult.getValue1());

                String alterName = alterNamespace.generateNewName();
                AssignStmt alterAssign = new AssignStmt();
                alterAssign.setLHS(new NameExpr(new Name(alterName)));
                alterAssign.setRHS(targetTransformResult.getValue0());
                alterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(alterAssign);

                targetExpr = new NameExpr(new Name(alterName));
            }

            resolveEndExpr(targetExpr);
            Pair<ast.List<Expr>, List<Stmt>> transformedArgument = copyAndTransformArgument();
            newPrefixStatementList.addAll(transformedArgument.getValue1());

            retExpr.setTarget(targetExpr.treeCopy());
            retExpr.setArgList(transformedArgument.getValue0());

            return new Pair<>(retExpr, newPrefixStatementList);
        }
    }

    private Pair<ast.List<Expr>, List<Stmt>> copyAndTransformArgument() {
        assert originalNode instanceof ParameterizedExpr;
        if (!hasFurtherTransformArgument()) {
            ast.List<Expr> retList = ((ParameterizedExpr) originalNode).getArgList().treeCopy();
            return new Pair<>(retList, new LinkedList<>());
        }
        ast.List<Expr> newArgList = new ast.List<>();
        List<Stmt> newPrefixStatementList = new LinkedList<>();
        for (ExprTrans exprTrans : argumentTransformerList) {
            Pair<Expr, List<Stmt>> argumentTransformResult = exprTrans.copyAndTransform();
            newPrefixStatementList.addAll(argumentTransformResult.getValue1());
            if (argumentTransformResult.getValue0() instanceof ColonExpr) {
                newArgList.add(new ColonExpr());
                assert argumentTransformResult.getValue1().isEmpty();
                continue;
            }
            if (hasFixLengthOutput(argumentTransformResult.getValue0())) {
                /* [expr]   <=>     [expr(transform)]   */
                /*                  t0 = [expr];        */
                /*                  return t0           */
                String t0Name = alterNamespace.generateNewName();

                AssignStmt t0Assign = new AssignStmt();
                t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                t0Assign.setRHS(argumentTransformResult.getValue0());
                t0Assign.setOutputSuppressed(true);
                newPrefixStatementList.add(t0Assign);

                newArgList.add(new NameExpr(new Name(t0Name)));
            } else {
                /* [expr]   <=>     [expr(transform)]   */
                /*                  t0 = {[expr1]};     */
                /*                  return t0{:}        */
                String t0Name = alterNamespace.generateNewName();

                AssignStmt t0Assign = new AssignStmt();
                t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                t0Assign.setRHS(new CellArrayExpr(new ast.List<>(new Row(new ast.List<>(argumentTransformResult.getValue0())))));
                t0Assign.setOutputSuppressed(true);
                newPrefixStatementList.add(t0Assign);

                newArgList.add(new CellIndexExpr(
                        new NameExpr(new Name(t0Name)),
                        new ast.List<Expr>(new ColonExpr())
                ));
            }
        }
        return new Pair<>(newArgList, newPrefixStatementList);
    }

    private void resolveEndExpr(NameExpr resolvedAs) {
        if (!hasEndExpression()) return;

        assert originalNode instanceof ParameterizedExpr;
        for (int argIndex = 0; argIndex < ((ParameterizedExpr) originalNode).getNumArg(); argIndex++) {
            final int MATLAB_INDEX = argIndex + 1;
            final int TOTAL_NUM_ARG = ((ParameterizedExpr) originalNode).getNumArg();

            ParameterizedExpr resolvedExpr = new ParameterizedExpr();
            resolvedExpr.setTarget(new NameExpr(new Name("builtin")));
            resolvedExpr.addArg(new StringLiteralExpr("end"));
            resolvedExpr.addArg(resolvedAs.treeCopy());
            resolvedExpr.addArg(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(MATLAB_INDEX))));
            resolvedExpr.addArg(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(TOTAL_NUM_ARG))));

            Consumer<ASTNode> subWorker = new Consumer<ASTNode>() {
                @Override
                public void accept(ASTNode astNode) {
                    if (astNode instanceof EndExpr) endExpressionResolveMap.put((EndExpr) astNode, resolvedExpr.treeCopy());
                    if (astNode instanceof CellIndexExpr) return;
                    if (astNode instanceof ParameterizedExpr) return;
                    for (int childIndex = 0; childIndex < astNode.getNumChild(); childIndex++) {
                        ASTNode child = astNode.getChild(childIndex);
                        this.accept(child);
                    }
                }
            };

            Expr argument = ((ParameterizedExpr) originalNode).getArg(argIndex);
            subWorker.accept(argument);
        }
    }

    private Pair<ast.List<Expr>, List<Stmt>> resolveColonExpr(ast.List<Expr> arguments, NameExpr resolvedAs) {
        boolean hasColonExpr = false;
        for (Expr argument : arguments) {
            if (argument instanceof ColonExpr) hasColonExpr = true;
        }
        if (!hasColonExpr) {
            return new Pair<>(arguments.treeCopy(), new LinkedList<>());
        }
        ast.List<Expr> newArgumentList = new ast.List<>();
        List<Stmt> newPrefixStatementList = new LinkedList<>();

        String totalNumArgName = alterNamespace.generateNewName();

        List<Expr> totalLengthCalcScalar = new LinkedList<>();
        for (Expr argument : arguments) {
            if (hasFixLengthOutput(argument)) {
                totalLengthCalcScalar.add(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
            } else {
                ParameterizedExpr lengthCalcExpr = new ParameterizedExpr();
                lengthCalcExpr.setTarget(new NameExpr(new Name("length")));
                lengthCalcExpr.addArg(new CellArrayExpr(new ast.List<>(new Row(new ast.List<>(argument.treeCopy())))));

                totalLengthCalcScalar.add(lengthCalcExpr);
            }
        }

        AssignStmt totalNumArgAssign = new AssignStmt();
        totalNumArgAssign.setLHS(new NameExpr(new Name(totalNumArgName)));
        totalNumArgAssign.setRHS(new ParameterizedExpr(
                new NameExpr(new Name("sum")),
                new ast.List<Expr>(builtMATLABScalarMatrix(totalLengthCalcScalar))
        ));
        totalNumArgAssign.setOutputSuppressed(true);
        newPrefixStatementList.add(totalNumArgAssign);

        for (int argIndex = 0; argIndex < arguments.getNumChild(); argIndex++) {
            Expr argument = arguments.getChild(argIndex);

            if (argument instanceof ColonExpr) {
                List<Expr> lengthSlice = totalLengthCalcScalar.subList(0, argIndex + 1);

                ParameterizedExpr endPosCalcExpr = new ParameterizedExpr();
                endPosCalcExpr.setTarget(new NameExpr(new Name("builtin")));
                endPosCalcExpr.addArg(new StringLiteralExpr("end"));
                endPosCalcExpr.addArg(resolvedAs.treeCopy());
                endPosCalcExpr.addArg(new ParameterizedExpr(
                        new NameExpr(new Name("sum")),
                        new ast.List<Expr>(builtMATLABScalarMatrix(lengthSlice))
                ));
                endPosCalcExpr.addArg(new NameExpr(new Name(totalNumArgName)));

                RangeExpr resolvedExpr = new RangeExpr();
                resolvedExpr.setLower(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
                resolvedExpr.setUpper(endPosCalcExpr);

                newArgumentList.add(resolvedExpr);
            } else {
                newArgumentList.add(argument.treeCopy());
            }
        }

        return new Pair<>(newArgumentList, newPrefixStatementList);
    }

    private boolean hasEndExpression() {
        assert originalNode instanceof ParameterizedExpr;
        Function<ASTNode, Boolean> recFinder = new Function<ASTNode, Boolean>() {
            @Override
            public Boolean apply(ASTNode astNode) {
                if (astNode instanceof EndExpr) return true;
                if (astNode instanceof CellIndexExpr) return false;
                if (astNode instanceof ParameterizedExpr) return false;
                boolean currentNodeResult = false;
                for (int childIndex = 0; childIndex < astNode.getNumChild(); childIndex++) {
                    boolean childResult = this.apply(astNode.getChild(childIndex));
                    currentNodeResult = currentNodeResult || childResult;
                }
                return currentNodeResult;
            }
        };
        for (Expr argument : ((ParameterizedExpr) originalNode).getArgList()) {
            if (recFinder.apply(argument).booleanValue() == true) return true;
        }
        return false;
    }

    private boolean hasColonExpression() {
        assert originalNode instanceof ParameterizedExpr;
        for (Expr argument : ((ParameterizedExpr) originalNode).getArgList()) {
            if (argument instanceof ColonExpr) return true;
        }
        return false;
    }

    /* helper functions */
    private MatrixExpr builtMATLABScalarMatrix(List<Expr> scalar) {
        Row row = new Row();
        for (Expr element : scalar) row.addElement(element.treeCopy());
        return new MatrixExpr(new ast.List<>(row));
    }

    private boolean hasFixLengthOutput(Expr expr) {
        if (expr instanceof CellIndexExpr) return false;
        if (expr instanceof DotExpr) return false;
        return true;
    }
}
