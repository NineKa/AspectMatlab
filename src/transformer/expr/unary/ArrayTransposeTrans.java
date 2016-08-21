package transformer.expr.unary;

import abstractPattern.analysis.PatternType;
import ast.*;
import org.javatuples.Pair;
import transformer.TransformerArgument;

import java.util.List;
import java.util.function.BiFunction;

public final class ArrayTransposeTrans extends UnaryTrans {
    public ArrayTransposeTrans(TransformerArgument argument, ArrayTransposeExpr arrayTransposeExpr) {
        super(argument, arrayTransposeExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        if (this.hasTransformOnCurrentNode()) {
            /* ([expr]).'   <=>     t0 = [expr]     */
            /*                      t1 = t0.'   *   */
            /*                      return t1       */
            BiFunction<LValueExpr, Expr, AssignStmt> buildAssignStmt = (LValueExpr lhs, Expr rhs) -> {
                AssignStmt returnStmt = new AssignStmt();
                returnStmt.setLHS(lhs);
                returnStmt.setRHS(rhs);
                returnStmt.setOutputSuppressed(true);
                return returnStmt;
            };

            Pair<Expr, List<Stmt>> operandTransformResult = this.operandTransformer.copyAndTransform();
            Expr copiedOperand = operandTransformResult.getValue0();
            List<Stmt> prefixStatementList = operandTransformResult.getValue1();

            String t0Name = this.alterNamespace.generateNewName();
            String t1Name = this.alterNamespace.generateNewName();

            AssignStmt t0Assign = buildAssignStmt.apply(new NameExpr(new Name(t0Name)), copiedOperand);
            AssignStmt t1Assign = buildAssignStmt.apply(
                    new NameExpr(new Name(t1Name)),
                    new ArrayTransposeExpr(new NameExpr(new Name(t0Name)))
            );

            this.jointPointDelegate.accept(new Pair<>(t1Assign, PatternType.Operator));
            prefixStatementList.add(t0Assign);
            prefixStatementList.add(t1Assign);

            return new Pair<>(new NameExpr(new Name(t1Name)), prefixStatementList);
        } else {
            ArrayTransposeExpr copiedNode = (ArrayTransposeExpr) this.originalNode.copy();
            Pair<Expr, List<Stmt>> operandTransformResult = this.operandTransformer.copyAndTransform();
            Expr copiedOperand = operandTransformResult.getValue0();
            copiedOperand.setParent(copiedNode);
            copiedNode.setOperand(copiedOperand);
            return new Pair<>(copiedNode, operandTransformResult.getValue1());
        }
    }
}
