package transformer.expr.lvalue;

import abstractPattern.analysis.PatternType;
import ast.*;
import natlab.toolkits.analysis.varorfun.VFAnalysis;
import natlab.toolkits.analysis.varorfun.VFDatum;
import org.javatuples.Pair;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;
import transformer.util.AccessMode;

import java.util.LinkedList;
import java.util.List;

public final class CellIndexTrans extends LValueTrans {
    private ExprTrans targetTranformer = null;
    private List<ExprTrans> argumentTransformerList = new LinkedList<>();

    public CellIndexTrans(ExprTransArgument argument, CellIndexExpr cellIndexExpr) {
        super(argument, cellIndexExpr);
        ExprTransArgument argArgument = argument.copy();
        argArgument.runtimeInfo.accessMode = AccessMode.Read;

        targetTranformer = ExprTrans.buildExprTransformer(argument, cellIndexExpr.getTarget());
        for (Expr argExpr : cellIndexExpr.getArgList()) {
            ExprTrans argTransformer = ExprTrans.buildExprTransformer(argArgument, argExpr);
            argumentTransformerList.add(argTransformer);
        }
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert originalNode instanceof CellIndexExpr;
        if (((CellIndexExpr) originalNode).getTarget() instanceof NameExpr) {
            if (hasTransformOnCurrentNode()) {
                VFAnalysis kindAnalysis = runtimeInfo.kindAnalysis;
                VFDatum kindAnalysisResult = kindAnalysis.getResult(((NameExpr) ((CellIndexExpr) originalNode).getTarget()).getName());
                if (kindAnalysisResult.isFunction()) {
                    /* a cell index expression can not be applied to a function */
                    assert false;       /* raise bug for kind analysis */
                }
                if (kindAnalysisResult.isVariable()) {
                    if (runtimeInfo.accessMode == AccessMode.Read) {
                        /* [expr1]{[expr2]}     <=>     t0 = [expr1]            *           */
                        /*                              return t0{[expr2]}                  */
                        List<Stmt> newPrefixStatementList = new LinkedList<>();

                        String t0Name = this.alterNamespace.generateNewName();
                        AssignStmt t0Assign = new AssignStmt();
                        t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                        t0Assign.setRHS(((CellIndexExpr) originalNode).getTarget().treeCopy());
                        t0Assign.setOutputSuppressed(true);
                        newPrefixStatementList.add(t0Assign);

                        Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArgument();

                        newPrefixStatementList.addAll(argumentTransformResult.getValue1());
                        CellIndexExpr retExpr = new CellIndexExpr();
                        retExpr.setTarget(new NameExpr(new Name(t0Name)));
                        retExpr.setArgList(argumentTransformResult.getValue0());

                        jointPointDelegate.accept(new Pair<>(t0Assign, PatternType.Get));

                        return new Pair<>(retExpr, newPrefixStatementList);
                    }

                    if (runtimeInfo.accessMode == AccessMode.Write) {
                        /* [expr1]{[expr2]}     <=>     if exist('[expr1]', 'var')          */
                        /*                                  t0 = [expr1]                    */
                        /*                              end                                 */
                        /*                              return t0{[expr2]}                  */
                        List<Stmt> newPrefixStatementList = new LinkedList<>();

                        Pair<ast.List<Expr>, List<Stmt>> argTransformResult = copyAndTransformArgument();

                        IfBlock copyBlcok = new IfBlock();
                        copyBlcok.setCondition(new ParameterizedExpr(
                                new NameExpr(new Name("exist")),
                                new ast.List<Expr>(
                                        new StringLiteralExpr(((NameExpr) ((CellIndexExpr) originalNode).getTarget()).getName().getID()),
                                        new StringLiteralExpr("var")
                                )
                        ));

                        String t0Name = this.alterNamespace.generateNewName();
                        AssignStmt t0Assign = new AssignStmt();
                        t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                        t0Assign.setRHS(((CellIndexExpr) originalNode).getTarget().treeCopy());
                        t0Assign.setOutputSuppressed(true);
                        copyBlcok.addStmt(t0Assign);

                        newPrefixStatementList.add(new IfStmt(new ast.List<IfBlock>(copyBlcok), new Opt<>()));
                        newPrefixStatementList.addAll(argTransformResult.getValue1());

                        CellIndexExpr retExpr = new CellIndexExpr();
                        retExpr.setTarget(new NameExpr(new Name(t0Name)));
                        retExpr.setArgList(argTransformResult.getValue0());

                        return new Pair<>(retExpr, newPrefixStatementList);
                    }

                    /* control flow should not reach here */
                    throw new AssertionError();
                }
                if (kindAnalysisResult.isID()) {
                    assert runtimeInfo.accessMode == AccessMode.Read;
                    /* transform as a variable get                                      */
                    /* [expr1]{[expr2]}     <=>     t0 = [expr1]            *           */
                    /*                              return t0{[expr2]}                  */
                    List<Stmt> newPrefixStatementList = new LinkedList<>();

                    String t0Name = this.alterNamespace.generateNewName();
                    AssignStmt t0Assign = new AssignStmt();
                    t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                    t0Assign.setRHS(((CellIndexExpr) originalNode).getTarget().treeCopy());
                    t0Assign.setOutputSuppressed(true);
                    newPrefixStatementList.add(t0Assign);

                    Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArgument();

                    newPrefixStatementList.addAll(argumentTransformResult.getValue1());
                    CellIndexExpr retExpr = new CellIndexExpr();
                    retExpr.setTarget(new NameExpr(new Name(t0Name)));
                    retExpr.setArgList(argumentTransformResult.getValue0());

                    jointPointDelegate.accept(new Pair<>(t0Assign, PatternType.Get));

                    return new Pair<>(retExpr, newPrefixStatementList);
                }
                /* control flow should not reach here */
                throw new AssertionError();
            } else {
                /* trivial transform */
                List<Stmt> newPrefixStatementList = new LinkedList<>();
                Pair<ast.List<Expr>, List<Stmt>> argTransformResult = copyAndTransformArgument();
                newPrefixStatementList.addAll(argTransformResult.getValue1());

                CellIndexExpr retExpr = new CellIndexExpr();
                retExpr.setTarget(((CellIndexExpr) originalNode).getTarget().treeCopy());
                retExpr.setArgList(argTransformResult.getValue0());

                return new Pair<>(retExpr, newPrefixStatementList);
            }
        } else {
            assert !hasTransformOnCurrentNode();

            List<Stmt> newPrefixStatementList = new LinkedList<>();

            Pair<Expr, List<Stmt>> targetTransformResult = targetTranformer.copyAndTransform();
            Pair<ast.List<Expr>, List<Stmt>> argumentTransformResult = copyAndTransformArgument();

            Expr copiedTarget = targetTransformResult.getValue0();

            newPrefixStatementList.addAll(targetTransformResult.getValue1());
            if (!argumentTransformResult.getValue1().isEmpty()) {
                String targetAlterName = this.alterNamespace.generateNewName();
                AssignStmt targetAlterAssign = new AssignStmt();
                targetAlterAssign.setLHS(new NameExpr(new Name(targetAlterName)));
                targetAlterAssign.setRHS(copiedTarget);
                targetAlterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(targetAlterAssign);
                copiedTarget = new NameExpr(new Name(targetAlterName));
            }
            newPrefixStatementList.addAll(argumentTransformResult.getValue1());

            CellIndexExpr retExpr = new CellIndexExpr();
            retExpr.setTarget(copiedTarget);
            retExpr.setArgList(argumentTransformResult.getValue0());

            return new Pair<>(retExpr, newPrefixStatementList);
        }
    }

    public Pair<ast.List<Expr>, List<Stmt>> copyAndTransformArgument() {
        assert originalNode instanceof CellIndexExpr;
        if (hasFurtherTransformArgument()) {
            List<Stmt> newPrefixStatementList = new LinkedList<>();
            ast.List<Expr> retCopiedNode = new ast.List<>();

            for (ExprTrans exprTrans : argumentTransformerList) {
                Pair<Expr, List<Stmt>> argTransformedResult = exprTrans.copyAndTransform();
                newPrefixStatementList.addAll(argTransformedResult.getValue1());

                String exprAlterName = this.alterNamespace.generateNewName();
                AssignStmt exprAlterAssign = new AssignStmt();
                exprAlterAssign.setLHS(new NameExpr(new Name(exprAlterName)));
                exprAlterAssign.setRHS(argTransformedResult.getValue0());
                exprAlterAssign.setOutputSuppressed(true);
                newPrefixStatementList.add(exprAlterAssign);

                retCopiedNode.add(new NameExpr(new Name(exprAlterName)));
            }
            return new Pair<>(retCopiedNode, newPrefixStatementList);
        } else {
            ast.List<Expr> copiedNode = ((CellIndexExpr) originalNode).getArgList().treeCopy();
            return new Pair<>(copiedNode, new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        if (this.hasTransformOnCurrentNode()) return true;
        assert originalNode instanceof CellIndexExpr;
        if (((CellIndexExpr) originalNode).getTarget() instanceof NameExpr) {
            assert !this.hasTransformOnCurrentNode();
            if (hasFurtherTransformArgument()) return true;
            return false;
        } else {
            if (targetTranformer.hasFurtherTransform()) return true;
            if (hasFurtherTransformArgument()) return true;
            return false;
        }
    }

    public boolean hasFurtherTransformArgument() {
        for (ExprTrans argTransformer : argumentTransformerList) {
            if (argTransformer.hasFurtherTransform()) return true;
        }
        return false;
    }
}
