package transformer.expr.lvalue;

import ast.*;
import natlab.DecIntNumericLiteralValue;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;
import transformer.jointpoint.AMJointPointGet;
import transformer.util.AccessMode;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public final class CellIndexTrans extends LValueTrans {
    private ExprTrans targetTransformer = null;
    private List<ExprTrans> argumentTransformerList = new LinkedList<>();

    public CellIndexTrans(TransformerArgument argument, CellIndexExpr cellIndexExpr) {
        super(argument, cellIndexExpr);
        assert cellIndexExpr.getNumArg() != 0;

        targetTransformer = ExprTrans.buildExprTransformer(argument, cellIndexExpr.getTarget());

        TransformerArgument argArgument = argument.copy();
        argArgument.runtimeInfo.accessMode = AccessMode.Read;
        for (Expr arg : cellIndexExpr.getArgList()) {
            ExprTrans argTransformer = ExprTrans.buildExprTransformer(argArgument, arg);
            argumentTransformerList.add(argTransformer);
        }

        /* populate end expr substitution map */
        if (hasTransformOnCurrentNode()) {
            assert cellIndexExpr.getTarget() instanceof NameExpr;
            String targetName = ((NameExpr) cellIndexExpr.getTarget()).getName().getID();
            int totalNumArgument = cellIndexExpr.getNumArg();
            for (int argIndex = 0; argIndex < cellIndexExpr.getNumArg(); argIndex++) {
                int matlabIndex = argIndex + 1;

                ParameterizedExpr substituteExpr = new ParameterizedExpr();
                substituteExpr.setTarget(new NameExpr(new Name("builtin")));
                substituteExpr.addArg(new StringLiteralExpr("end"));
                substituteExpr.addArg(new NameExpr(new Name(targetName)));
                substituteExpr.addArg(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(matlabIndex))));
                substituteExpr.addArg(new IntLiteralExpr(new DecIntNumericLiteralValue(String.valueOf(totalNumArgument))));

                Consumer<ASTNode> collectEndExpr = new Consumer<ASTNode>() {
                    @Override
                    public void accept(ASTNode astNode) {
                        if (astNode instanceof EndExpr) endExpressionResolveMap.put((EndExpr) astNode, substituteExpr);
                        if (astNode instanceof ParameterizedExpr) return;
                        if (astNode instanceof CellIndexExpr) return;
                        for (int childIndex = 0; childIndex < astNode.getNumChild(); childIndex++) {
                            ASTNode childNode = astNode.getChild(childIndex);
                            this.accept(childNode);
                        }
                    }
                };

                collectEndExpr.accept(cellIndexExpr.getArg(argIndex));
            }
        }
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert originalNode instanceof CellIndexExpr;
        CellIndexExpr retExpr = new CellIndexExpr();
        List<Stmt> newPrefixStatementList = new LinkedList<>();
        if (hasTransformOnCurrentNode()) {
            assert ((CellIndexExpr) originalNode).getTarget() instanceof NameExpr;
            String targetName = ((NameExpr) ((CellIndexExpr) originalNode).getTarget()).getName().getID();
            /* [expr1]{[expr2]}     <=>     [expr2(transform)]          */
            /*                              t0 = {[expr2]}              */
            /*                              t1 = {[expr1]{t0{:}}    *   */
            /*                              return t1{:}                */
            Pair<ast.List<Expr>, List<Stmt>> argTransformResult = copyAndTransformArgument();
            newPrefixStatementList.addAll(argTransformResult.getValue1());

            String t0Name = alterNamespace.generateNewName();
            AssignStmt t0Assign = new AssignStmt();
            t0Assign.setLHS(new NameExpr(new Name(t0Name)));
            t0Assign.setRHS(new CellArrayExpr(new ast.List<>(new Row(argTransformResult.getValue0()))));
            t0Assign.setOutputSuppressed(true);
            newPrefixStatementList.add(t0Assign);

            String t1Name = alterNamespace.generateNewName();
            AssignStmt t1Assign = new AssignStmt();
            t1Assign.setLHS(new NameExpr(new Name(t1Name)));
            t1Assign.setRHS(new CellArrayExpr(new ast.List<>(new Row(new ast.List<>(new CellIndexExpr(
                    new NameExpr(new Name(targetName)),
                    new ast.List<Expr>(new CellIndexExpr(
                            new NameExpr(new Name(t0Name)),
                            new ast.List<Expr>(new ColonExpr())
                    ))
            ))))));
            t1Assign.setOutputSuppressed(true);
            newPrefixStatementList.add(t1Assign);

            retExpr = new CellIndexExpr(new NameExpr(new Name(t1Name)), new ast.List<>(new ColonExpr()));

            /* invoke joint point delegate function */
            AMJointPointGet jointPoint = new AMJointPointGet(
                    t1Assign, originalNode.getStartLine(),
                    originalNode.getStartColumn(), enclosingFilename
            );
            jointPoint.addAllMatchedAction(getPossibleAttachedActionsSet());
            jointPoint.setTargetExpr(new NameExpr(new Name(targetName)));
            jointPoint.setIndicesExpr(new NameExpr(new Name(t0Name)));
            jointPointDelegate.accept(jointPoint);

        } else {
            ast.List<Expr> argumentList;
            if (hasFurtherTransformArgument()) {
                Pair<ast.List<Expr>, List<Stmt>> transformResult = copyAndTransformArgumentNoColonResolve();
                argumentList = transformResult.getValue0();
                newPrefixStatementList.addAll(transformResult.getValue1());
            } else {
                argumentList = ((CellIndexExpr) originalNode).getArgList().treeCopy();
            }
            assert argumentList != null;
            retExpr.setArgList(argumentList);

            Expr targetExpr;
            if (((CellIndexExpr) originalNode).getTarget() instanceof NameExpr) {
                String targetName = ((NameExpr) ((CellIndexExpr) originalNode).getTarget()).getName().getID();
                targetExpr = new NameExpr(new Name(targetName));
            } else {
                Pair<Expr, List<Stmt>> transformResult = targetTransformer.copyAndTransform();
                targetExpr = transformResult.getValue0();
                newPrefixStatementList.addAll(transformResult.getValue1());
            }
            assert targetExpr != null;
            retExpr.setTarget(targetExpr);
        }

        return new Pair<>(retExpr, newPrefixStatementList);
    }

    @Override
    public boolean hasFurtherTransform() {
        return false;
    }

    private Pair<ast.List<Expr>, List<Stmt>> copyAndTransformArgument() {
        ast.List<Expr> newArgList = new ast.List<>();
        List<Stmt> newPrefixStatementList = new LinkedList<>();

        /* first pass transform all arguments, and contain them in variables */
        for (ExprTrans exprTrans : argumentTransformerList) {
            Pair<Expr, List<Stmt>> transformResult = exprTrans.copyAndTransform();
            newPrefixStatementList.addAll(transformResult.getValue1());
            /* handling colon expr */
            if (transformResult.getValue0() instanceof ColonExpr) {
                newArgList.add(new ColonExpr());
                continue;
            }
            /* handling spanning comma separated list */
            if ((transformResult.getValue0() instanceof CellIndexExpr) || (transformResult.getValue0() instanceof DotExpr)) {
                /* [expr]{[expr2]}  <=>     [expr2(transform prefix)]       */
                /*                          t0 = {[expr]{[expr2]}}          */
                /*                          return t0{:}                    */

                /* [expr].[name]    <=>     [expr(transform)]               */
                /*                          t0 = {[expr].[name]}            */
                /*                          return t0{:}                    */
                String t0Name = alterNamespace.generateNewName();
                AssignStmt t0Assign = new AssignStmt();
                t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                t0Assign.setRHS(new CellArrayExpr(new ast.List<>(
                        new Row(new ast.List<>(transformResult.getValue0().treeCopy()))
                )));
                t0Assign.setOutputSuppressed(true);

                newPrefixStatementList.add(t0Assign);
                newArgList.add(new CellIndexExpr(new NameExpr(new Name(t0Name)), new ast.List<>(new ColonExpr())));

                continue;
            }
            /* otherwise use trivial transformation */
            /*  [expr]              <=>     t0 = [expr]                 */
            /*                              return t0                   */
            String t0Name = alterNamespace.generateNewName();
            AssignStmt t0Assign = new AssignStmt();
            t0Assign.setLHS(new NameExpr(new Name(t0Name)));
            t0Assign.setRHS(transformResult.getValue0().treeCopy());
            t0Assign.setOutputSuppressed(true);

            newPrefixStatementList.add(t0Assign);
            newArgList.add(new NameExpr(new Name(t0Name)));
        }

        /* second pass - calculating total number of arguments */
        Row lengthMatrixScalar = new Row();
        for (Expr argument : newArgList) {
            assert !(argument instanceof DotExpr);
            if (argument instanceof CellIndexExpr) {    /* variable length */
                assert ((CellIndexExpr) argument).getTarget() instanceof NameExpr;
                String wrappedName = ((NameExpr) ((CellIndexExpr) argument).getTarget()).getName().getID();

                ParameterizedExpr calcuatingExpr = new ParameterizedExpr();
                calcuatingExpr.setTarget(new NameExpr(new Name("length")));
                calcuatingExpr.addArg(new NameExpr(new Name(wrappedName)));
                lengthMatrixScalar.addElement(calcuatingExpr);
            } else {                                    /* fixed length    */
                lengthMatrixScalar.addElement(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
            }
        }
        MatrixExpr numArgMatrix = new MatrixExpr(new ast.List<>(lengthMatrixScalar));

        String totalArgName = alterNamespace.generateNewName();
        AssignStmt totalArgAssign = new AssignStmt();
        totalArgAssign.setLHS(new NameExpr(new Name(totalArgName)));
        totalArgAssign.setRHS(new ParameterizedExpr(new NameExpr(
                new Name("sum")),
                new ast.List<Expr>(numArgMatrix)
        ));
        totalArgAssign.setOutputSuppressed(true);
        newPrefixStatementList.add(totalArgAssign);

        /* third pass - resolve colon expr */
        assert originalNode instanceof CellIndexExpr;
        assert ((CellIndexExpr) originalNode).getTarget() instanceof NameExpr;
        String targetName = ((NameExpr) ((CellIndexExpr) originalNode).getTarget()).getName().getID();

        ast.List<Expr> colonExprResolvedArgList = new ast.List<>();
        int indexCounter = 0;
        for (Expr argument : newArgList) {
            indexCounter = indexCounter + 1;
            if (argument instanceof ColonExpr) {
                Row currentIndexCalcExpr = new Row();
                for (int copyIndex = 0; copyIndex < indexCounter; copyIndex++) {
                    currentIndexCalcExpr.addElement(lengthMatrixScalar.getElement(copyIndex).treeCopy());
                }
                MatrixExpr currentIndexCalcMatrix = new MatrixExpr(new ast.List<>(currentIndexCalcExpr));

                ParameterizedExpr currentIndexResultExpr = new ParameterizedExpr();
                currentIndexResultExpr.setTarget(new NameExpr(new Name("sum")));
                currentIndexResultExpr.addArg(currentIndexCalcMatrix);

                RangeExpr resolvedRangeExpr = new RangeExpr();
                resolvedRangeExpr.setLower(new IntLiteralExpr(new DecIntNumericLiteralValue("1")));
                resolvedRangeExpr.setUpper(new ParameterizedExpr(
                        new NameExpr(new Name("builtin")),
                        new ast.List<Expr>(
                                new StringLiteralExpr("end"),
                                new NameExpr(new Name(targetName)),
                                currentIndexResultExpr,
                                new NameExpr(new Name(totalArgName))
                        )
                ));

                colonExprResolvedArgList.add(resolvedRangeExpr);
            } else {
                colonExprResolvedArgList.add(argument);
            }
        }

        return new Pair<>(colonExprResolvedArgList, newPrefixStatementList);
    }

    private Pair<ast.List<Expr>, List<Stmt>> copyAndTransformArgumentNoColonResolve() {
        ast.List<Expr> newArgList = new ast.List<>();
        List<Stmt> newPrefixStatementList = new LinkedList<>();

        /* first pass transform all arguments, and contain them in variables */
        for (ExprTrans exprTrans : argumentTransformerList) {
            Pair<Expr, List<Stmt>> transformResult = exprTrans.copyAndTransform();
            newPrefixStatementList.addAll(transformResult.getValue1());
            /* handling colon expr */
            if (transformResult.getValue0() instanceof ColonExpr) {
                newArgList.add(new ColonExpr());
                continue;
            }
            /* handling spanning comma separated list */
            if ((transformResult.getValue0() instanceof CellIndexExpr) || (transformResult.getValue0() instanceof DotExpr)) {
                /* [expr]{[expr2]}  <=>     [expr2(transform prefix)]       */
                /*                          t0 = {[expr]{[expr2]}}          */
                /*                          return t0{:}                    */

                /* [expr].[name]    <=>     [expr(transform)]               */
                /*                          t0 = {[expr].[name]}            */
                /*                          return t0{:}                    */
                String t0Name = alterNamespace.generateNewName();
                AssignStmt t0Assign = new AssignStmt();
                t0Assign.setLHS(new NameExpr(new Name(t0Name)));
                t0Assign.setRHS(new CellArrayExpr(new ast.List<>(
                        new Row(new ast.List<>(transformResult.getValue0().treeCopy()))
                )));
                t0Assign.setOutputSuppressed(true);

                newPrefixStatementList.add(t0Assign);
                newArgList.add(new CellIndexExpr(new NameExpr(new Name(t0Name)), new ast.List<>(new ColonExpr())));

                continue;
            }
            /* otherwise use trivial transformation */
            /*  [expr]              <=>     t0 = [expr]                 */
            /*                              return t0                   */
            String t0Name = alterNamespace.generateNewName();
            AssignStmt t0Assign = new AssignStmt();
            t0Assign.setLHS(new NameExpr(new Name(t0Name)));
            t0Assign.setRHS(transformResult.getValue0().treeCopy());
            t0Assign.setOutputSuppressed(true);

            newPrefixStatementList.add(t0Assign);
            newArgList.add(new NameExpr(new Name(t0Name)));
        }

        return new Pair<>(newArgList, newPrefixStatementList);
    }

    private boolean hasFurtherTransformArgument() {
        for (ExprTrans exprTrans : argumentTransformerList) {
            if (exprTrans.hasFurtherTransform()) return true;
        }
        return false;
    }
}
