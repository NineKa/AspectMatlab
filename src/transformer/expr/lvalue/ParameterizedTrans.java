package transformer.expr.lvalue;

import abstractPattern.Primitive;
import abstractPattern.analysis.PatternType;
import ast.*;
import natlab.DecIntNumericLiteralValue;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import natlab.toolkits.analysis.varorfun.VFDatum;
import org.javatuples.Pair;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;
import transformer.util.AccessMode;
import transformer.util.IsPossibleJointPointResult;

import java.util.LinkedList;
import java.util.List;

public final class ParameterizedTrans extends LValueTrans {
    private ExprTrans targetTransformer = null;
    private List<ExprTrans> argumentTransformerList = new LinkedList<>();

    public ParameterizedTrans(ExprTransArgument argument, ParameterizedExpr parameterizedExpr) {
        super(argument, parameterizedExpr);

        ExprTransArgument argArgument = argument.copy();
        argArgument.runtimeInfo.accessMode = AccessMode.Read;

        targetTransformer = ExprTrans.buildExprTransformer(argument, parameterizedExpr.getTarget());
        for (Expr arg : parameterizedExpr.getArgList()) {
            argumentTransformerList.add(ExprTrans.buildExprTransformer(argArgument, arg));
        }
    }

    @Override
    public Pair<Expr, java.util.List<Stmt>> copyAndTransform() {
        assert originalNode instanceof ParameterizedExpr;
        if (((ParameterizedExpr) originalNode).getTarget() instanceof NameExpr) {
            if (hasTransformOnCurrentNode()) {
                VFAnalysis kindAnalysis = runtimeInfo.kindAnalysis;
                VFDatum kindAnalysisResult = kindAnalysis.getResult(((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName());

                if (kindAnalysisResult.isFunction()) {
                    /* match to a function call pattern */
                    /*  [expr1]([expr2..])  <=>     t0 = {[expr2..]}            */
                    /*                              t1 = [expr1](t0{:})     *   */
                    /*                              return t1                   */
                    List<Stmt> newPrefixStatementList = new LinkedList<>();
                    Pair<ast.List<Expr>, List<Stmt>> argumentListTransformResult = copyAndTransformArguments();
                    newPrefixStatementList.addAll(argumentListTransformResult.getValue1());

                    String t0Name = this.alterNamespace.generateNewName();
                    AssignStmt t0Assign = new AssignStmt();
                    t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                    t0Assign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(argumentListTransformResult.getValue0()))));
                    t0Assign.setOutputSuppressed(true);
                    newPrefixStatementList.add(t0Assign);

                    String t1Name = this.alterNamespace.generateNewName();
                    AssignStmt t1Assign = new AssignStmt();
                    t1Assign.setLHS(new NameExpr(new Name(t1Name)));
                    t1Assign.setRHS(new ParameterizedExpr(
                            (NameExpr) ((ParameterizedExpr) originalNode).getTarget().treeCopy(),
                            new ast.List<Expr>(new CellIndexExpr(new NameExpr(new Name(t0Name)), new ast.List<Expr>(new ColonExpr())))
                    ));
                    t1Assign.setOutputSuppressed(true);
                    newPrefixStatementList.add(t1Assign);

                    this.jointPointDelegate.accept(new Pair<>(t1Assign, PatternType.Call));

                    return new Pair<>(new NameExpr(new Name(t1Name)), newPrefixStatementList);
                }

                if (kindAnalysisResult.isVariable()) {
                    if (runtimeInfo.accessMode == AccessMode.Read) {
                        /* [expr1]([expr2..])   <=>     t0 = [expr1]         *  */
                        /*                              return t0([expr2..])    */
                        List<Stmt> newPrefixStatementList = new LinkedList<>();
                        Pair<ast.List<Expr>, List<Stmt>> argumentListTransformResult = copyAndTransformArguments();

                        String t0Name = this.alterNamespace.generateNewName();
                        AssignStmt t0Assign = new AssignStmt();
                        t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                        t0Assign.setRHS((NameExpr) ((ParameterizedExpr) originalNode).getTarget().treeCopy());
                        t0Assign.setOutputSuppressed(true);
                        newPrefixStatementList.add(t0Assign);

                        newPrefixStatementList.addAll(argumentListTransformResult.getValue1());
                        ParameterizedExpr retExpr = new ParameterizedExpr(
                                new NameExpr(new Name(t0Name)),
                                argumentListTransformResult.getValue0()
                        );

                        this.jointPointDelegate.accept(new Pair<>(t0Assign, PatternType.Get));

                        return new Pair<>(new NameExpr(new Name(retExpr)),  newPrefixStatementList);
                    }

                    if (runtimeInfo.accessMode == AccessMode.Write) {
                        /* [expr1]([expr2..])   <=>     if exist('[expr1]', 'var')      */
                        /*                                  t0 = [expr1]                */
                        /*                              end                             */
                        /*                              return t0([expr2..])            */
                        List<Stmt> newPrefixStatementList = new LinkedList<>();
                        Pair<ast.List<Expr>, List<Stmt>> argumentTransform = copyAndTransformArguments();

                        IfBlock checkingBlock = new IfBlock();
                        checkingBlock.setCondition(new ParameterizedExpr(
                                new NameExpr(new Name("exist")),
                                new ast.List<Expr>(
                                        new StringLiteralExpr(((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName().getID()),
                                        new StringLiteralExpr("var")
                                )
                        ));
                        String t0Name = this.alterNamespace.generateNewName();
                        AssignStmt t0Assign = new AssignStmt();
                        t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                        t0Assign.setRHS((NameExpr) ((ParameterizedExpr) originalNode).getTarget().treeCopy());
                        t0Assign.setOutputSuppressed(true);
                        checkingBlock.addStmt(t0Assign);
                        newPrefixStatementList.add(new IfStmt(new ast.List<>(checkingBlock), new Opt<>()));

                        newPrefixStatementList.addAll(argumentTransform.getValue1());

                        ParameterizedExpr retExpr = new ParameterizedExpr(
                                new NameExpr(new Name(t0Name)),
                                argumentTransform.getValue0()
                        );
                        return new Pair<>(retExpr, newPrefixStatementList);
                    }

                    /* control flow should not reach here */
                    throw new AssertionError();
                }

                if (kindAnalysisResult.isID()) {
                    assert runtimeInfo.accessMode == AccessMode.Read;
                    /* [expr1]([expr2..])   <=>     if any(cellfun(@(x)(exist('[expr1]') == x), {5,3,6,2,4,8}))     */
                    /*                                  t1 = {[expr2..]}                                            */
                    /*                                  t2 = [expr1](t1{:})                          *(optional)    */
                    /*                              else                                                            */
                    /*                                  t1 = [expr1]                                 *(optional)    */
                    /*                                  t2 = t1([expr2..])                                          */
                    /*                              end                                                             */
                    /*                              return t2                                                       */

                    List<Stmt> newPrefixStatementList = new LinkedList<>();

                    LambdaExpr existTypeCheckLambdaFunc = new LambdaExpr();
                    existTypeCheckLambdaFunc.addInputParam(new Name("x"));
                    existTypeCheckLambdaFunc.setBody(new EQExpr(
                            new ParameterizedExpr(
                                    new NameExpr(new Name("exist")),
                                    new ast.List<Expr>(
                                            new StringLiteralExpr(((NameExpr) ((ParameterizedExpr) originalNode).getTarget()).getName().getID())
                                    )),
                            new NameExpr(new Name("x"))
                    ));


                    ParameterizedExpr existCallExpr = new ParameterizedExpr(
                            new NameExpr(new Name("cellfun")),
                            new ast.List<Expr>(
                                    existTypeCheckLambdaFunc,
                                    new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                                            new IntLiteralExpr(new DecIntNumericLiteralValue("5")),
                                            new IntLiteralExpr(new DecIntNumericLiteralValue("3")),
                                            new IntLiteralExpr(new DecIntNumericLiteralValue("6")),
                                            new IntLiteralExpr(new DecIntNumericLiteralValue("2")),
                                            new IntLiteralExpr(new DecIntNumericLiteralValue("4")),
                                            new IntLiteralExpr(new DecIntNumericLiteralValue("8"))
                                    ))))
                            )
                    );

                    /* building function call block */
                    IfBlock functionCallBlock = new IfBlock();
                    functionCallBlock.setCondition(new ParameterizedExpr(
                            new NameExpr(new Name("any")),
                            new ast.List<Expr>(existCallExpr)
                    ));

                    Pair<ast.List<Expr>, List<Stmt>> funcArgTransformResult = copyAndTransformArguments();
                    for (Stmt stmt : funcArgTransformResult.getValue1()) functionCallBlock.addStmt(stmt);

                    String t1Name = this.alterNamespace.generateNewName();
                    AssignStmt funct1Assign = new AssignStmt();
                    funct1Assign.setLHS(new NameExpr(new Name(t1Name)));
                    funct1Assign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(funcArgTransformResult.getValue0()))));
                    funct1Assign.setOutputSuppressed(true);
                    functionCallBlock.addStmt(funct1Assign);

                    String t2Name = this.alterNamespace.generateNewName();
                    AssignStmt funct2Assign = new AssignStmt();
                    funct2Assign.setLHS(new NameExpr(new Name(t2Name)));
                    funct2Assign.setRHS(new ParameterizedExpr(
                            (NameExpr) ((ParameterizedExpr) originalNode).getTarget().treeCopy(),
                            new ast.List<Expr>(new CellIndexExpr(
                                    new NameExpr(new Name(t1Name)),
                                    new ast.List<Expr>(new ColonExpr())
                            ))
                    ));
                    funct2Assign.setOutputSuppressed(true);
                    functionCallBlock.addStmt(funct2Assign);

                    /* building variable get block */
                    ElseBlock variableGetBlock = new ElseBlock();

                    Pair<ast.List<Expr>, List<Stmt>> varArgTransformResult = copyAndTransformArguments();

                    AssignStmt vart1Assign = new AssignStmt();
                    vart1Assign.setLHS(new NameExpr(new Name(t1Name)));
                    vart1Assign.setRHS((NameExpr) ((ParameterizedExpr) originalNode).getTarget().treeCopy());
                    vart1Assign.setOutputSuppressed(true);
                    variableGetBlock.addStmt(vart1Assign);

                    for (Stmt stmt : varArgTransformResult.getValue1()) variableGetBlock.addStmt(stmt);

                    AssignStmt vart2Assign = new AssignStmt();
                    vart2Assign.setLHS(new NameExpr(new Name(t2Name)));
                    vart2Assign.setRHS(new ParameterizedExpr(
                            (NameExpr) ((ParameterizedExpr) originalNode).getTarget().treeCopy(),
                            varArgTransformResult.getValue0()
                    ));
                    vart2Assign.setOutputSuppressed(true);
                    variableGetBlock.addStmt(vart2Assign);

                    /* finalize if statement */
                    IfStmt appendingIfStmt = new IfStmt(new ast.List<>(functionCallBlock), new Opt<>(variableGetBlock));
                    newPrefixStatementList.add(appendingIfStmt);

                    boolean needReportCall = false;
                    boolean needReportGet  = false;
                    for (abstractPattern.Action action : this.actions) {
                        assert action.getPattern() instanceof Primitive;
                        Primitive pattern = (Primitive) action.getPattern();
                        IsPossibleJointPointResult query = pattern.isPossibleJointPoint(originalNode, runtimeInfo);
                        if (query.isCalls) needReportCall = true;
                        if (query.isGets)  needReportGet = true;
                        assert !query.isSets;
                    }
                    assert needReportCall || needReportGet;

                    if (needReportCall) jointPointDelegate.accept(new Pair<>(funct2Assign, PatternType.Call));
                    if (needReportGet) jointPointDelegate.accept(new Pair<>(vart1Assign, PatternType.Get));

                    return new Pair<>(new NameExpr(new Name(t2Name)), newPrefixStatementList);
                }

                /* control flow should not reach here */
                throw new AssertionError();
            } else {
                /* trivial return */
                List<Stmt> newPrefixStatementList = new LinkedList<>();

                Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArguments();
                newPrefixStatementList.addAll(argumentTransformResult.getValue1());

                ParameterizedExpr retExpr = new ParameterizedExpr(
                        (NameExpr) ((ParameterizedExpr) originalNode).getTarget().treeCopy(),
                        argumentTransformResult.getValue0()
                );
                return new Pair<>(retExpr, newPrefixStatementList);
            }
        } else {
            assert !hasTransformOnCurrentNode();

            List<Stmt> newPrefixStatementList = new LinkedList<>();

            Pair<Expr, List<Stmt>> targetTransformerResult = this.targetTransformer.copyAndTransform();
            Pair<ast.List<Expr>, List<Stmt>> argumentTransformerResult = copyAndTransformArguments();

            Expr copiedTarget = targetTransformerResult.getValue0();

            newPrefixStatementList.addAll(targetTransformerResult.getValue1());
            if (!argumentTransformerResult.getValue1().isEmpty()) {
                String targetAlterName = this.alterNamespace.generateNewName();
                AssignStmt targetAlterAssign = new AssignStmt();
                targetAlterAssign.setLHS(new NameExpr(new Name(targetAlterName)));
                targetAlterAssign.setRHS(copiedTarget);
                targetAlterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(targetAlterAssign);

                copiedTarget = new NameExpr(new Name(targetAlterName));
            }
            newPrefixStatementList.addAll(argumentTransformerResult.getValue1());

            ParameterizedExpr retExpr = new ParameterizedExpr(copiedTarget, argumentTransformerResult.getValue0());

            return new Pair<>(retExpr, newPrefixStatementList);
        }
    }

    public Pair<ast.List<Expr>, List<Stmt>> copyAndTransformArguments() {
        assert this.originalNode instanceof ParameterizedExpr;
        if (hasFurtherTransformArguments()) {
            List<Stmt> newPrefixStatementList = new LinkedList<>();
            ast.List<Expr> newArgList = new ast.List<>();

            for (ExprTrans argTrans : this.argumentTransformerList) {
                Pair<Expr, List<Stmt>> argTransformResult = argTrans.copyAndTransform();

                /* special case */
                if (argTransformResult.getValue0() instanceof CellIndexExpr) {
                    newArgList.add(argTransformResult.getValue0());
                    newPrefixStatementList.addAll(argTransformResult.getValue1());
                    continue;
                }

                String alterName = this.alterNamespace.generateNewName();

                AssignStmt alterAssign = new AssignStmt();
                alterAssign.setLHS(new NameExpr(new Name(alterName)));
                alterAssign.setRHS(argTransformResult.getValue0());
                alterAssign.setOutputSuppressed(true);

                newPrefixStatementList.addAll(argTransformResult.getValue1());
                newPrefixStatementList.add(alterAssign);

                newArgList.add(new NameExpr(new Name(alterName)));
            }

            return new Pair<>(newArgList, newPrefixStatementList);
        } else {
            ast.List<Expr> copiedNode = ((ParameterizedExpr) this.originalNode).getArgList().treeCopy();
            return new Pair<>(copiedNode, new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        if (this.hasTransformOnCurrentNode()) return true;

        assert originalNode instanceof ParameterizedExpr;
        if (((ParameterizedExpr) originalNode).getTarget() instanceof NameExpr) {
            return hasFurtherTransformArguments();
        } else {
            if (targetTransformer.hasFurtherTransform()) return true;
            if (hasFurtherTransformArguments()) return true;
            return false;
        }
    }


    public boolean hasFurtherTransformArguments() {
        for (ExprTrans exprTrans : this.argumentTransformerList) {
            if (exprTrans.hasFurtherTransform()) return true;
        }
        return false;
    }
}
