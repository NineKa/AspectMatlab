package transformer.expr.other;

import ast.*;
import org.javatuples.Pair;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;

public final class CellArrayTrans extends ExprTrans{
    private List<List<ExprTrans>> cellElementTransformer = new LinkedList<>();

    public CellArrayTrans(ExprTransArgument argument, CellArrayExpr cellArrayExpr) {
        super(argument, cellArrayExpr);
        for (Row row : cellArrayExpr.getRowList()) {
            List<ExprTrans> currentRowTransformer = new LinkedList<>();
            for (Expr element : row.getElementList()) {
                ExprTrans exprTransformer = ExprTrans.buildExprTransformer(argument, element);
                currentRowTransformer.add(exprTransformer);
            }
            cellElementTransformer.add(currentRowTransformer);
        }
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !hasTransformOnCurrentNode();
        List<Stmt> newPrefixStatementList = new LinkedList<>();
        CellArrayExpr newCellArrayExpr = new CellArrayExpr();

        /* Transform left two right, evaluation order */
        if (this.hasFurtherTransform()) {
            for (List<ExprTrans> rowIter : this.cellElementTransformer) {
                Row newRow = new Row();
                for (ExprTrans transIter : rowIter) {
                    Pair<Expr, List<Stmt>> transformResult = transIter.copyAndTransform();
                    Expr copiedElement = transformResult.getValue0();
                    newPrefixStatementList.addAll(transformResult.getValue1());

                    String exprAlterName = this.alterNamespace.generateNewName();
                    AssignStmt exprAlterAssign = new AssignStmt();
                    exprAlterAssign.setLHS(new NameExpr(new Name(exprAlterName)));
                    exprAlterAssign.setRHS(copiedElement);
                    exprAlterAssign.setOutputSuppressed(true);

                    newPrefixStatementList.add(exprAlterAssign);
                    newRow.addElement(new NameExpr(new Name(exprAlterName)));
                }
                newCellArrayExpr.addRow(newRow);
            }
            return new Pair<>(newCellArrayExpr, newPrefixStatementList);
        } else {
            return new Pair<>(this.originalNode.treeCopy(), new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        boolean needTransform = false;
        for (List<ExprTrans> rowIter : this.cellElementTransformer) {
            for (ExprTrans transIter : rowIter) {
                if (transIter.hasFurtherTransform()) needTransform = true;
            }
        }
        return needTransform;
    }
}
