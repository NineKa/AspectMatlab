package transformer.expr.binary;

import ast.*;
import org.javatuples.Pair;
import transformer.expr.ExprTransArgument;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public final class ShortCircuitOrTrans extends BinaryTrans {
    private final Expr TRUERETURNEXPR = new NameExpr(new Name("true"));

    public ShortCircuitOrTrans(ExprTransArgument argument, ShortCircuitOrExpr shortCircuitOrTrans) {
        super(argument, shortCircuitOrTrans);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !this.hasTransformOnCurrentNode();
        /*  [expr1] || [expr2]      <=>     t0 = expr1          */
        /*                                  if t0               */
        /*                                      t1 = true       */
        /*                                  else                */
        /*                                      t1 = expr2      */
        /*                                  end                 */
        /*                                  return t1           */

        if (!this.LHSTransformer.hasFurtherTransform() && !this.RHSTransformer.hasFurtherTransform()) {
            /* trivial case => return [lhs] && [rhs] directly */
            Pair<Expr, List<Stmt>> lhsTransformResult = this.LHSTransformer.copyAndTransform();
            Pair<Expr, List<Stmt>> rhsTransformResult = this.RHSTransformer.copyAndTransform();
            Expr copiedLHS = lhsTransformResult.getValue0();
            Expr copiedRHS = rhsTransformResult.getValue0();

            assert lhsTransformResult.getValue1().isEmpty();
            assert rhsTransformResult.getValue1().isEmpty();
            List<Stmt> newPrefixStatementList = new LinkedList<>();

            ShortCircuitOrExpr copiedNode = (ShortCircuitOrExpr) this.originalNode.copy();

            copiedLHS.setParent(copiedNode);
            copiedRHS.setParent(copiedNode);
            copiedNode.setLHS(copiedLHS);
            copiedNode.setRHS(copiedRHS);

            return new Pair<>(copiedNode, newPrefixStatementList);
        }
        BiFunction<LValueExpr, Expr, AssignStmt> buildAssignStmt = (LValueExpr lhs, Expr rhs) -> {
            AssignStmt returnStmt = new AssignStmt();
            returnStmt.setLHS(lhs);
            returnStmt.setRHS(rhs);
            returnStmt.setOutputSuppressed(true);
            return returnStmt;
        };

        Pair<Expr, List<Stmt>> lhsTransformResult = this.LHSTransformer.copyAndTransform();
        Pair<Expr, List<Stmt>> rhsTransformResult = this.RHSTransformer.copyAndTransform();
        Expr copiedLHS = lhsTransformResult.getValue0();
        Expr copiedRHS = rhsTransformResult.getValue0();

        String t0Name = this.alterNamespace.generateNewName();
        String t1Name = this.alterNamespace.generateNewName();

        List<Stmt> newPrefixStatementList = new LinkedList<>();
        for (Stmt iter : lhsTransformResult.getValue1()) newPrefixStatementList.add(iter);

        AssignStmt t0Assign = buildAssignStmt.apply(new NameExpr(new Name(t0Name)), copiedLHS);
        newPrefixStatementList.add(t0Assign);
        /* building if block */
        ast.List<Stmt> ifBlockStatement = new ast.List<>();
        AssignStmt t1IfAssign = buildAssignStmt.apply(new NameExpr(new Name(t1Name)), TRUERETURNEXPR);
        ifBlockStatement.add(t1IfAssign);
        IfBlock ifBlock = new IfBlock(new NameExpr(new Name(t0Name)), ifBlockStatement);
        /* building else block */
        ast.List<Stmt> elseBlockStatement = new ast.List<>();
        for (Stmt iter : rhsTransformResult.getValue1()) elseBlockStatement.add(iter);
        AssignStmt t1ElseAssign = buildAssignStmt.apply(new NameExpr(new Name(t1Name)), copiedRHS);
        elseBlockStatement.add(t1ElseAssign);
        ElseBlock elseBlock = new ElseBlock(elseBlockStatement);
        /* building If Statement */
        IfStmt ifStmt = new IfStmt(new ast.List<>(ifBlock), new Opt<>(elseBlock));

        newPrefixStatementList.add(ifStmt);
        return new Pair<>(new NameExpr(new Name(t1Name)), newPrefixStatementList);
    }
}
