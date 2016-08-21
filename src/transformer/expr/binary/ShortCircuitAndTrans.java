package transformer.expr.binary;

import ast.*;
import org.javatuples.Pair;
import transformer.TransformerArgument;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public final class ShortCircuitAndTrans extends BinaryTrans {
    private static final Expr FALSERETURNEXPR = new NameExpr(new Name("false"));

    public ShortCircuitAndTrans(TransformerArgument argument, ShortCircuitAndExpr shortCircuitAndExpr) {
        super(argument, shortCircuitAndExpr);
    }

    @Override
    public Pair<Expr, List<Stmt>> copyAndTransform() {
        assert !this.hasTransformOnCurrentNode();
        /* [expr1] && [expr2]       <=>     t0 = expr1          */
        /*                                  if t0               */
        /*                                      t1 = expr2      */
        /*                                  else                */
        /*                                      t1 = false      */
        /*                                  end                 */
        /*                                  return t1           */
        if (!this.LHSTransformer.hasFurtherTransform() && !this.RHSTransformer.hasFurtherTransform()) {
            /* trivial case => return [lhs] && [rhs] directly */
            Pair<Expr, List<Stmt>> lhsTransformResult = this.LHSTransformer.copyAndTransform();
            Pair<Expr, List<Stmt>> rhsTransformResult = this.RHSTransformer.copyAndTransform();

            assert lhsTransformResult.getValue1().isEmpty();
            assert rhsTransformResult.getValue1().isEmpty();

            Expr copiedLHS = lhsTransformResult.getValue0();
            Expr copiedRHS = rhsTransformResult.getValue0();
            ShortCircuitAndExpr copiedNode = (ShortCircuitAndExpr) this.originalNode.copy();

            copiedLHS.setParent(copiedNode);
            copiedRHS.setParent(copiedNode);
            copiedNode.setLHS(copiedLHS);
            copiedNode.setRHS(copiedRHS);

            return new Pair<>(copiedNode, new LinkedList<>());
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
        /* building If Block */
        ast.List<Stmt> ifBlockStatement = new ast.List<>();
        for (Stmt iter : rhsTransformResult.getValue1()) ifBlockStatement.add(iter);
        AssignStmt t1IfAssign = buildAssignStmt.apply(new NameExpr(new Name(t1Name)), copiedRHS);
        ifBlockStatement.add(t1IfAssign);
        IfBlock ifBlock = new IfBlock(new NameExpr(new Name(t0Name)), ifBlockStatement);
        /* building Else Block */
        ast.List<Stmt> elseBlockStatement = new ast.List<>();
        AssignStmt t1ElseAssign = buildAssignStmt.apply(new NameExpr(new Name(t1Name)), FALSERETURNEXPR);
        elseBlockStatement.add(t1ElseAssign);
        ElseBlock elseBlock = new ElseBlock(elseBlockStatement);
        /* building If Statement */
        IfStmt ifStmt = new IfStmt(new ast.List<>(ifBlock), new Opt<>(elseBlock));

        newPrefixStatementList.add(ifStmt);
        return new Pair<>(new NameExpr(new Name(t1Name)), newPrefixStatementList);
    }
}
