package transformer.expr.lvalue;

import ast.*;
import natlab.DecIntNumericLiteralValue;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;
import transformer.jointpoint.AMJointPointGet;
import transformer.jointpoint.AMJointPointSet;
import transformer.util.AccessMode;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class CellIndexTrans extends LValueTrans {
    private ExprTrans targetTransformer = null;
    private List<ExprTrans> argumentTransformerList = new LinkedList<>();

    public CellIndexTrans(TransformerArgument argument, CellIndexExpr cellIndexExpr) {
        super(argument, cellIndexExpr);

        targetTransformer = ExprTrans.buildExprTransformer(argument, cellIndexExpr.getTarget());

        TransformerArgument argArgument = argument.copy();
        argArgument.runtimeInfo.accessMode = AccessMode.Read;

        for (Expr arg : cellIndexExpr.getArgList()) {
            ExprTrans appendingTrans = ExprTrans.buildExprTransformer(argArgument, arg);
            argumentTransformerList.add(appendingTrans);
        }
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

    private Pair<Expr, List<Stmt>> accessModeReadHandle() {
        assert runtimeInfo.accessMode == AccessMode.Read;
        assert originalNode instanceof CellIndexExpr;
        CellIndexExpr retExpr = new CellIndexExpr();
        List<Stmt> newPrefixStatementList = new LinkedList<>();

        if (hasTransformOnCurrentNode()) {
            assert ((CellIndexExpr) originalNode).getTarget() instanceof NameExpr;
            String targetName = ((NameExpr) ((CellIndexExpr) originalNode).getTarget()).getName().getID();
            if (hasEndExpression() || hasColonExpression()) {
                resolveEndExpr(new NameExpr(new Name(targetName)));
                Pair<ast.List<Expr>, List<Stmt>> argTransResult = copyAndTransformArgument();
                Pair<ast.List<Expr>, List<Stmt>> colonResuloveResult = resolveColonExpr(
                        argTransResult.getValue0(),
                        new NameExpr(new Name(targetName))
                );
                newPrefixStatementList.addAll(argTransResult.getValue1());
                newPrefixStatementList.addAll(colonResuloveResult.getValue1());

                ast.List<Expr> newArgumentList = colonResuloveResult.getValue0();

                String alterName = alterNamespace.generateNewName();
                AssignStmt alterAssign = new AssignStmt();
                alterAssign.setLHS(new NameExpr(new Name(alterName)));
                alterAssign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                        new CellIndexExpr(new NameExpr( new Name(targetName)), newArgumentList)
                )))));
                alterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(alterAssign);

                /* invoke joint point delegate */
                AMJointPointGet jointPoint = new AMJointPointGet(
                        alterAssign, originalNode.getStartLine(),
                        originalNode.getStartColumn(), enclosingFilename
                );
                jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
                jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
                jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(newArgumentList))));
                jointPointDelegate.accept(jointPoint);

                retExpr.setTarget(new NameExpr(new Name(alterName)));
                retExpr.addArg(new ColonExpr());

                return new Pair<>(retExpr, newPrefixStatementList);
            } else {
                Pair<ast.List<Expr>, List<Stmt>> argTransResult = copyAndTransformArgument();
                newPrefixStatementList.addAll(argTransResult.getValue1());

                String alterName = alterNamespace.generateNewName();
                AssignStmt alterAssign = new AssignStmt();
                alterAssign.setLHS(new NameExpr(new Name(alterName)));
                alterAssign.setRHS(new CellIndexExpr(new NameExpr(new Name(targetName)), argTransResult.getValue0()));
                alterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(alterAssign);

                /* invoke joint point delegate */
                AMJointPointGet jointPoint = new AMJointPointGet(
                        alterAssign, originalNode.getStartLine(),
                        originalNode.getStartColumn(), enclosingFilename
                );
                jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
                jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
                jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                        new CellArrayExpr(new ast.List<Row>(new Row(argTransResult.getValue0().treeCopy())))
                )))));
                jointPointDelegate.accept(jointPoint);

                retExpr.setTarget(new NameExpr(new Name(alterName)));
                retExpr.addArg(new ColonExpr());

                return new Pair<>(retExpr, newPrefixStatementList);
            }
        } else {
            if (!hasFurtherTransform()) {
                Expr copiedExpr = originalNode.treeCopy();
                return new Pair<>(copiedExpr, new LinkedList<>());
            }
            NameExpr targetExpr;
            if (((CellIndexExpr) originalNode).getTarget() instanceof NameExpr) {
                targetExpr = (NameExpr) ((CellIndexExpr) originalNode).getTarget().treeCopy();
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

            ast.List<Expr> argumentList;
            if (hasFurtherTransformArgument()) {
                resolveEndExpr(targetExpr);
                Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArgument();

                newPrefixStatementList.addAll(argumentTransformResult.getValue1());

                argumentList = argumentTransformResult.getValue0();
            } else {
                argumentList = ((CellIndexExpr) originalNode).getArgList().treeCopy();
            }

            retExpr.setTarget(targetExpr);
            retExpr.setArgList(argumentList);

            return new Pair<>(retExpr, newPrefixStatementList);
        }
    }

    @SuppressWarnings("deprecation")
    private Pair<Expr, List<Stmt>> accessModeWriteHandle() {
        assert runtimeInfo.accessMode == AccessMode.Write;
        assert originalNode instanceof CellIndexExpr;
        CellIndexExpr retExpr = new CellIndexExpr();
        List<Stmt> newPrefixStatement = new LinkedList<>();
        if (hasTransformOnCurrentNode()) {
            assert ((CellIndexExpr) originalNode).getTarget() instanceof NameExpr;
            String targetName = ((NameExpr) ((CellIndexExpr) originalNode).getTarget()).getName().getID();
            resolveEndExpr(new NameExpr(new Name(targetName)));
            /* [expr1]{[expr2]}     <=>     if exist('[expr1]','var')       */
            /*                                  t0 = [expr1];               */
            /*                              end                             */
            /*                              [expr2(transform)]              */
            /*                              t0{[expr2]}                 =   */
            /*                              [expr1]{[expr2]} = t0{[expr2]}; */

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

            newPrefixStatement.add(new IfStmt(new ast.List<IfBlock>(existingCheckBlock), new Opt<ElseBlock>()));

            Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArgument();
            newPrefixStatement.addAll(argumentTransformResult.getValue1());
            Pair<ast.List<Expr>, List<Stmt>> colonResolveTransformResult = resolveColonExpr(
                    argumentTransformResult.getValue0(),
                    new NameExpr(new Name(t0Name))
            );
            newPrefixStatement.addAll(colonResolveTransformResult.getValue1());

            retExpr.setTarget(new NameExpr(new Name(t0Name)));
            retExpr.setArgList(colonResolveTransformResult.getValue0());

            AssignStmt t0RetrieveAssign = new AssignStmt();
            t0RetrieveAssign.setLHS(new CellIndexExpr(
                    new NameExpr(new Name(targetName)),
                    colonResolveTransformResult.getValue0().treeCopy()
            ));
            t0RetrieveAssign.setRHS(new CellIndexExpr(
                    new NameExpr(new Name(t0Name)),
                    colonResolveTransformResult.getValue0().treeCopy()
            ));
            t0RetrieveAssign.setOutputSuppressed(true);
            assignRetriveStack.push(t0RetrieveAssign);

            /* invoke joint point delegate */
            AMJointPointSet jointPoint = new AMJointPointSet(
                    t0RetrieveAssign, originalNode.getStartLine(),
                    originalNode.getStartColumn(), enclosingFilename
            );
            jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
            jointPoint.setIndicesExpr(new CellArrayExpr(new ast.List<Row>(new Row(
                    colonResolveTransformResult.getValue0().treeCopy()
            ))));
            jointPoint.setNewVarExpr(new NameExpr(new Name(t0Name)));
            jointPoint.setOldVarExpr(new NameExpr(new Name(targetName)));
            jointPointDelegate.accept(jointPoint);

            return new Pair<>(retExpr, newPrefixStatement);
        } else {
            assert originalNode instanceof CellIndexExpr;
            if (!hasFurtherTransform()) {
                CellIndexExpr copiedExpr = (CellIndexExpr) originalNode.treeCopy();
                return new Pair<>(copiedExpr, new LinkedList<>());
            }
            if (hasEndExpression()) {
                NameExpr targetExpr;
                if (((CellIndexExpr) originalNode).getTarget() instanceof NameExpr) {
                    targetExpr = (NameExpr) ((CellIndexExpr) originalNode).getTarget().treeCopy();
                } else {
                    /* [expr1]{[expr2]}     <=>     [expr1(transform)]          */
                    /*                              t0 = [expr1];               */
                    /*                              targetExpr = t0;            */
                    Pair<Expr, List<Stmt>> targetTransformResult = targetTransformer.copyAndTransform();
                    newPrefixStatement.addAll(targetTransformResult.getValue1());

                    String t0Name = alterNamespace.generateNewName();
                    AssignStmt t0Assign = new AssignStmt();
                    t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                    t0Assign.setRHS(targetTransformResult.getValue0().treeCopy());
                    t0Assign.setOutputSuppressed(true);
                    newPrefixStatement.add(t0Assign);

                    targetExpr = new NameExpr(new Name(t0Name));

                    AssignStmt t0RetrieveAssign = new AssignStmt();
                    t0RetrieveAssign.setLHS(targetTransformResult.getValue0().treeCopy());
                    t0RetrieveAssign.setRHS(new NameExpr(new Name(t0Name)));
                    t0RetrieveAssign.setOutputSuppressed(true);
                    assignRetriveStack.push(t0RetrieveAssign);
                }
                resolveEndExpr(targetExpr);

                Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArgument();
                newPrefixStatement.addAll(argumentTransformResult.getValue1());

                retExpr.setTarget(targetExpr);
                retExpr.setArgList(argumentTransformResult.getValue0());

                return new Pair<>(retExpr, newPrefixStatement);
            } else {
                Expr targetExp;
                if (((CellIndexExpr) originalNode).getTarget() instanceof NameExpr) {
                    targetExp = ((CellIndexExpr) originalNode).getTarget().treeCopy();
                } else {
                    Pair<Expr, List<Stmt>> targetTransformResult = targetTransformer.copyAndTransform();
                    newPrefixStatement.addAll(targetTransformResult.getValue1());
                    targetExp = targetTransformResult.getValue0();
                }


                Pair<ast.List<Expr>, List<Stmt>> argumentListTransformResult = copyAndTransformArgument();
                newPrefixStatement.addAll(argumentListTransformResult.getValue1());

                retExpr.setTarget(targetExp);
                retExpr.setArgList(argumentListTransformResult.getValue0());

                return new Pair<>(retExpr, newPrefixStatement);
            }
        }
    }

    public boolean hasFurtherTransform() {
        assert originalNode instanceof CellIndexExpr;
        if (((CellIndexExpr) originalNode).getTarget() instanceof NameExpr) {
            if (hasTransformOnCurrentNode()) return true;
            if (hasFurtherTransformArgument()) return true;
            return false;
        } else {
            if (targetTransformer.hasFurtherTransform()) return true;
            if (hasFurtherTransformArgument()) return true;
            return false;
        }
    }

    public boolean hasFurtherTransformArgument() {
        assert originalNode instanceof CellIndexExpr;
        for (ExprTrans argumentTransformer : argumentTransformerList) {
            if (argumentTransformer.hasFurtherTransform()) return true;
        }
        return false;
    }

    private boolean hasEndExpression() {
        assert originalNode instanceof CellIndexExpr;
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
        for (Expr argument : ((CellIndexExpr) originalNode).getArgList()) {
            if (recFinder.apply(argument)) return true;
        }
        return false;
    }

    private boolean hasColonExpression() {
        assert originalNode instanceof CellIndexExpr;
        for (Expr argument : ((CellIndexExpr) originalNode).getArgList()) {
            if (argument instanceof ColonExpr) return true;
        }
        return false;
    }

    private void resolveEndExpr(NameExpr resolvedAs) {
        assert hasEndExpression();
        assert originalNode instanceof CellIndexExpr;
        for (int argIndex = 0; argIndex < ((CellIndexExpr) originalNode).getNumArg(); argIndex++) {
            final int MATLAB_INDEX = argIndex + 1;
            final int TOTAL_NUM_ARG = ((CellIndexExpr) originalNode).getNumArg();

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

            Expr argument = ((CellIndexExpr) originalNode).getArg(argIndex);
            subWorker.accept(argument);
        }
    }

    private Pair<ast.List<Expr>, List<Stmt>> copyAndTransformArgument() {
        assert originalNode instanceof CellIndexExpr;
        ast.List<Expr> newArgumentList = new ast.List<>();
        List<Stmt> newPrefixStatementList = new LinkedList<>();

        if (!hasFurtherTransformArgument()) {
            newArgumentList = ((CellIndexExpr) originalNode).getArgList().treeCopy();
            return new Pair<>(newArgumentList, new LinkedList<>());
        }

        for (ExprTrans argumentTransformer : argumentTransformerList) {
            Pair<Expr, List<Stmt>> transformResult = argumentTransformer.copyAndTransform();
            newPrefixStatementList.addAll(transformResult.getValue1());
            if (transformResult.getValue0() instanceof ColonExpr) {
                assert transformResult.getValue1().isEmpty();
                newArgumentList.add(new ColonExpr());
                continue;
            }

            if (hasFixLengthOutput(transformResult.getValue0())) {
                /* [expr]       <=>     [expr(transform)]           */
                /*                      t0 = [expr];                */
                /*                      return t0                   */
                String alterName = alterNamespace.generateNewName();

                AssignStmt alterAssign = new AssignStmt();
                alterAssign.setLHS(new NameExpr(new Name(alterName)));
                alterAssign.setRHS(transformResult.getValue0());
                alterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(alterAssign);

                newArgumentList.add(new NameExpr(new Name(alterName)));
                continue;
            } else {
                /* [expr]       <=>     [expr(transform)]           */
                /*                      t0 = {[expr]};              */
                /*                      return t0{:}                */
                String alterName = alterNamespace.generateNewName();

                AssignStmt alterAssign = new AssignStmt();
                alterAssign.setLHS(new NameExpr(new Name(alterName)));
                alterAssign.setRHS(new CellArrayExpr(new ast.List<>(new Row(new ast.List<>(transformResult.getValue0())))));
                alterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(alterAssign);

                newArgumentList.add(new CellIndexExpr(
                        new NameExpr(new Name(alterName)),
                        new ast.List<Expr>(new ColonExpr())
                ));
                continue;
            }
        }
        return new Pair<>(newArgumentList, newPrefixStatementList);
    }

    private Pair<ast.List<Expr>, List<Stmt>> resolveColonExpr(ast.List<Expr> arguments, NameExpr resolvedAs) {
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
