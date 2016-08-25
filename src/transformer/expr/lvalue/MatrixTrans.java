package transformer.expr.lvalue;

import ast.*;
import org.javatuples.Pair;
import transformer.TransformerArgument;
import transformer.expr.ExprTrans;
import transformer.util.AccessMode;

import java.util.LinkedList;
import java.util.List;

public final class MatrixTrans extends LValueTrans {
    private List<List<ExprTrans>> elementTransformerList = new LinkedList();

    public MatrixTrans(TransformerArgument argument, MatrixExpr matrixExpr) {
        super(argument, matrixExpr);

        for (Row row : matrixExpr.getRowList()) {
            List<ExprTrans> appendRowTransformerList = new LinkedList<>();
            for (Expr element : row.getElementList()) {
                ExprTrans elementTrans = ExprTrans.buildExprTransformer(argument, element);
                appendRowTransformerList.add(elementTrans);
            }
            elementTransformerList.add(appendRowTransformerList);
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

    private Pair<Expr, List<Stmt>> accessModeWriteHandle() {
        assert runtimeInfo.accessMode == AccessMode.Write;
        assert !hasTransformOnCurrentNode();
        assert originalNode instanceof MatrixExpr;

        MatrixExpr retExpr = new MatrixExpr();
        List<Stmt> newPrefixStatementList = new LinkedList<>();

        for (List<ExprTrans> rowIter : elementTransformerList) {
            Row appendingRow = new Row();
            for (ExprTrans elementTransformer : rowIter) {
                Pair<Expr, List<Stmt>> elementTransformResult = elementTransformer.copyAndTransform();
                newPrefixStatementList.addAll(elementTransformResult.getValue1());

                appendingRow.addElement(elementTransformResult.getValue0());
            }
            retExpr.addRow(appendingRow);
        }

        return new Pair<>(retExpr, newPrefixStatementList);
    }

    private Pair<Expr, List<Stmt>> accessModeReadHandle() {
        assert runtimeInfo.accessMode == AccessMode.Read;
        assert !hasTransformOnCurrentNode();
        assert originalNode instanceof MatrixExpr;
        MatrixExpr retExpr = new MatrixExpr();
        List<Stmt> newPrefixStatementList = new LinkedList<>();
        if (hasFurtherTransform()) {
            for (List<ExprTrans> rowIter : elementTransformerList) {
                Row appendingRow = new Row();
                for (ExprTrans elementTransformer : rowIter) {
                    Pair<Expr, List<Stmt>> elementTransformResult = elementTransformer.copyAndTransform();
                    newPrefixStatementList.addAll(elementTransformResult.getValue1());

                    if (hasFixLengthOutput(elementTransformResult.getValue0())) {
                        String alterName = alterNamespace.generateNewName();
                        AssignStmt alterAssign = new AssignStmt();
                        alterAssign.setLHS(new NameExpr(new Name(alterName)));
                        alterAssign.setRHS(elementTransformResult.getValue0());
                        alterAssign.setOutputSuppressed(true);
                        newPrefixStatementList.add(alterAssign);

                        appendingRow.addElement(new NameExpr(new Name(alterName)));
                    } else {
                        String alterName = alterNamespace.generateNewName();
                        AssignStmt alterAssign = new AssignStmt();
                        alterAssign.setLHS(new NameExpr(new Name(alterName)));
                        alterAssign.setRHS(new CellArrayExpr(new ast.List<Row>(new Row(new ast.List<Expr>(
                                elementTransformResult.getValue0()
                        )))));
                        alterAssign.setOutputSuppressed(true);
                        newPrefixStatementList.add(alterAssign);

                        appendingRow.addElement(new CellIndexExpr(
                                new NameExpr(new Name(alterName)),
                                new ast.List<Expr>(new ColonExpr())
                        ));
                    }
                }
                retExpr.addRow(appendingRow);
            }
            return new Pair<>(retExpr, newPrefixStatementList);
        } else {
            retExpr = (MatrixExpr) originalNode.treeCopy();
            return new Pair<>(retExpr, new LinkedList<>());
        }
    }

    @Override
    public boolean hasFurtherTransform() {
        for (List<ExprTrans> rowIter : elementTransformerList) {
            for (ExprTrans elementIter : rowIter) {
                if (elementIter.hasFurtherTransform()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasFixLengthOutput(Expr expr) {
        if (expr instanceof CellIndexExpr) return false;
        if (expr instanceof DotExpr) return false;
        return true;
    }
}
