package transformer.expr.other;

import ast.*;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;

import java.util.LinkedList;
import java.util.List;

public final class CellArrayTrans extends ExprTrans{
    private List<List<ExprTrans>> cellElementTransformer = new LinkedList<>();

    public CellArrayTrans(TransformerArgument argument, CellArrayExpr cellArrayExpr) {
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
                    newPrefixStatementList.addAll(transformResult.getValue1());
                    /* special case */
                    if (hasFixLengthOutput(transformResult.getValue0())) {
                        String alterName = alterNamespace.generateNewName();
                        AssignStmt alterAssign = new AssignStmt();
                        alterAssign.setLHS(new NameExpr(new Name(alterName)));
                        alterAssign.setRHS(transformResult.getValue0());
                        alterAssign.setOutputSuppressed(true);
                        newPrefixStatementList.add(alterAssign);

                        newRow.addElement(new NameExpr(new Name(alterName)));
                    } else {
                        String alterName = alterNamespace.generateNewName();
                        AssignStmt alterAssign = new AssignStmt();
                        alterAssign.setLHS(new NameExpr(new Name(alterName)));
                        alterAssign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                                transformResult.getValue0()
                        )))));
                        alterAssign.setOutputSuppressed(true);
                        newPrefixStatementList.add(alterAssign);

                        newRow.addElement(new CellIndexExpr(
                                new NameExpr(new Name(alterName)),
                                new ast.List<Expr>(new ColonExpr())
                        ));
                    }

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

    private boolean hasFixLengthOutput(Expr expr) {
        if (expr instanceof CellIndexExpr) return false;
        if (expr instanceof DotExpr) return false;
        return true;
    }
}
