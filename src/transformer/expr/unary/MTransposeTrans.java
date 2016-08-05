package transformer.expr.unary;

import abstractPattern.Action;
import abstractPattern.Primitive;
import ast.Expr;
import ast.MTransposeExpr;
import ast.Name;
import ast.NameExpr;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;
import util.Namespace;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class MTransposeTrans extends UnaryTrans {

    public MTransposeTrans(Collection<Action> actions, RuntimeInfo runtimeInfo, Namespace namespace, MTransposeExpr expr) {
        super(actions, runtimeInfo, namespace, expr);
    }

    @Override
    public boolean hasFutureTransform() {
        /* check if operand need transform */
        if (this.operandTransformer.hasFutureTransform()) return true;

        for (Action action : this.actions) {
            assert action.getPattern() instanceof Primitive;
            Primitive primitivePattern = (Primitive) action.getPattern();
            IsPossibleJointPointResult query = primitivePattern.isPossibleJointPoint(this.originalNode, this.runtimeInfo);
            if (query.isPossible()) return true;
        }

        return false;
    }

    @Override
    public Pair<Expr, List<Triplet<String, Expr, Boolean>>> transform() {
        /*
        * ([expr])'    <=>      t_1 = [expr]
        *                       t_2 = (t_1)'    *
        *                       t_2
        */
        Expr copiedOperand = null;
        List<Triplet<String, Expr, Boolean>> transformMap = null;
        if (this.operandTransformer.hasFutureTransform()) {
            Pair<Expr, List<Triplet<String, Expr, Boolean>>> result = this.operandTransformer.transform();
            copiedOperand = result.getValue0();
            transformMap = result.getValue1();
        } else {
            copiedOperand = this.operandExpr.treeCopy();
            transformMap = new LinkedList<>();
        }
        assert copiedOperand != null && transformMap != null;
        /* decide if such pattern is possible a Joint Point */
        boolean isPossibleJointPoint = false;
        for (Action action : this.actions) {
            assert action.getPattern() instanceof Primitive;
            Primitive primitivePattern = (Primitive) action.getPattern();
            IsPossibleJointPointResult query = primitivePattern.isPossibleJointPoint(this.originalNode, this.runtimeInfo);
            if (query.isPossible()) isPossibleJointPoint = true;
        }
        if (isPossibleJointPoint) {     /* perform the transformation    */
            String t1AlterName = this.alterNamespace.generateNewName();
            Expr t1AlterExpr = copiedOperand;
            Triplet<String, Expr, Boolean> t1 = new Triplet<>(t1AlterName, t1AlterExpr, false);

            String t2AlterName = this.alterNamespace.generateNewName();
            Expr t2AlterExpr = this.originalNode.treeCopy();
            assert t2AlterExpr instanceof MTransposeExpr;
            ((MTransposeExpr) t2AlterExpr).setOperand(new NameExpr(new Name(t1AlterName)));
            Triplet<String, Expr, Boolean> t2 = new Triplet<>(t2AlterName, t2AlterExpr, true);

            transformMap.add(t1);
            transformMap.add(t2);

            /* building return Pair */
            Expr retExpr = new NameExpr(new Name(t2AlterName));
            List<Triplet<String, Expr, Boolean>> retTransformMap = transformMap;

            return new Pair<>(retExpr, retTransformMap);
        } else {                        /* copy and return original node */
            Expr retExpr = this.originalNode.treeCopy();
            assert retExpr instanceof MTransposeExpr;
            ((MTransposeExpr) retExpr).setOperand(copiedOperand);
            List<Triplet<String, Expr, Boolean>> retTransformMap = transformMap;

            return new Pair<>(retExpr, retTransformMap);
        }
    }

    @Override
    public Class<? extends Expr> correspondAST() {
        return MTransposeExpr.class;
    }

}
