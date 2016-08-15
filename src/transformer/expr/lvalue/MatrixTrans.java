package transformer.expr.lvalue;

import ast.*;
import org.javatuples.Pair;
import transformer.expr.ExprTrans;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;

public final class MatrixTrans extends LValueTrans{
    private List<List<ExprTrans>> elementTransformerList = new LinkedList<>();

    public MatrixTrans(ExprTransArgument argument, MatrixExpr matrixExpr) {
        super(argument, matrixExpr);
        for (Row row : matrixExpr.getRowList()) {
            List<ExprTrans> rowTransformerList = new LinkedList<>();
            for (Expr element : row.getElementList()) {
                ExprTrans elementTransformer = ExprTrans.buildExprTransformer(argument, element);
                rowTransformerList.add(elementTransformer);
            }
            elementTransformerList.add(rowTransformerList);
        }
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        if (this.hasFurtherTransform()) {
            List<Stmt> newPrefixStatementList = new LinkedList<>();
            MatrixExpr retExpr = new MatrixExpr();

            for (List<ExprTrans> rowIterator : elementTransformerList) {
                Row newRow = new Row();
                for (ExprTrans elementIterator : rowIterator) {
                    Pair<Expr, List<Stmt>> transformResult = elementIterator.copyAndTransform();

                    /* special case */
                    if (transformResult.getValue0() instanceof CellIndexExpr) {
                        newPrefixStatementList.addAll(transformResult.getValue1());
                        newRow.addElement(transformResult.getValue0());
                        continue;
                    }

                    newPrefixStatementList.addAll(transformResult.getValue1());

                    String alterName = alterNamespace.generateNewName();
                    AssignStmt alterAssign = new AssignStmt();
                    alterAssign.setLHS(new NameExpr(new Name(alterName)));
                    alterAssign.setRHS(transformResult.getValue0());
                    alterAssign.setOutputSuppressed(true);
                    newPrefixStatementList.add(alterAssign);

                    newRow.addElement(new NameExpr(new Name(alterName)));
                }
                retExpr.addRow(newRow);
            }
            return new Pair<>(retExpr, newPrefixStatementList);
        } else {
            return new Pair<>(originalNode.treeCopy(), new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        assert !hasTransformOnCurrentNode();
        for (List<ExprTrans> rowIterator : elementTransformerList) {
            for (ExprTrans elementIterator : rowIterator) {
                if (elementIterator.hasFurtherTransform()) return true;
            }
        }
        return false;
    }
}
