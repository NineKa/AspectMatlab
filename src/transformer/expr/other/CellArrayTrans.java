package transformer.expr.other;

import ast.CellArrayExpr;
import ast.Expr;
import ast.Row;
import ast.Stmt;
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
                ExprTrans exprTransformer = ExprTrans.buildExprTransformer(this.originalArgument, element);
                currentRowTransformer.add(exprTransformer);
            }
            cellElementTransformer.add(currentRowTransformer);
        }
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {  /* TODO */
        assert !hasTransformOnCurrentNode();
        List<Stmt> newPrefixStatementList = new LinkedList<>();
        assert this.originalNode instanceof CellArrayExpr;

        /* Transform left two right, evaluation order */
        if (this.hasFurtherTransform()) {
            for (List<ExprTrans> rowIter : this.cellElementTransformer) {
                for (ExprTrans transIter : rowIter) {

                }
            }
            return null;
        } else {
            return null;
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
