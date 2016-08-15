package transformer.expr;

import ast.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.javatuples.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Aspect
public class ExprTransOptimizeAspect {
    @Around("execution(* transformer.expr.ExprTrans.copyAndTransform(..))")
    @SuppressWarnings("deprecation")
    public Object optimizeAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        Object transformResult = joinPoint.proceed();
        assert transformResult instanceof Pair;
        assert ((Pair) transformResult).getValue0() instanceof Expr;
        assert ((Pair) transformResult).getValue1() instanceof List;
        assert joinPoint.getThis() instanceof ExprTrans;

        ExprTrans transformer = (ExprTrans) joinPoint.getThis();
        ExprTransArgument transformArgument = transformer.getTransformArgument();

        Pair<Expr, List<Stmt>> returnValue = (Pair) transformResult;
        returnValue = literalSubstitute(transformArgument, returnValue);
        returnValue = equivalentSubstitute(transformArgument, returnValue);
        return returnValue;
    }

    public static Pair<Expr, List<Stmt>> literalSubstitute(ExprTransArgument argument, Pair<Expr, List<Stmt>> candidate) {
        List<Stmt> newPrefixStatementList = new LinkedList<>();

        Map<String, LiteralExpr> substituteMap = new HashMap<>();
        for (Stmt stmt : candidate.getValue1()) {
            if (!(stmt instanceof AssignStmt)) { newPrefixStatementList.add(stmt); continue; }
            if (!(((AssignStmt) stmt).getLHS() instanceof NameExpr)) { newPrefixStatementList.add(stmt); continue; }
            if (!(((AssignStmt) stmt).getRHS() instanceof LiteralExpr)) { newPrefixStatementList.add(stmt); continue; }
            String variableName = ((NameExpr) ((AssignStmt) stmt).getLHS()).getName().getID();
            if (!argument.alterNamespace.getDefinedNameSet().contains(variableName)) { newPrefixStatementList.add(stmt); continue; }
            LiteralExpr literalExpr = (LiteralExpr) ((AssignStmt) stmt).getRHS();
            substituteMap.put(variableName, literalExpr);
        }

        java.util.function.Function<ASTNode, ASTNode> transform = new Function<ASTNode, ASTNode>() {
            @Override
            public ASTNode apply(ASTNode astNode) {
                if (astNode instanceof ast.NameExpr) {
                    String currentName = ((NameExpr) astNode).getName().getID();
                    if (substituteMap.containsKey(currentName)) {
                        return substituteMap.get(currentName).treeCopy();
                    } else {
                        return astNode;
                    }
                }
                for (int iter = 0; iter < astNode.getNumChild(); iter++) {
                    astNode.setChild(apply(astNode.getChild(iter)), iter);
                }
                return astNode;
            }
        };

        List<Stmt> retList = new LinkedList<>();
        for (Stmt stmt : newPrefixStatementList) {
            retList.add((Stmt)transform.apply(stmt));
        }

        Expr retExpr = (Expr) transform.apply(candidate.getValue0());

        return new Pair<>(retExpr, retList);
    }

    public static Pair<Expr, List<Stmt>> equivalentSubstitute(ExprTransArgument argument, Pair<Expr, List<Stmt>> candidate) {
        List<Stmt> newPrefixStatementList = new LinkedList<>();
        Map<String, String> substituteMap = new HashMap<>();

        for (Stmt stmt : candidate.getValue1()) {
            if (!(stmt instanceof AssignStmt)) {newPrefixStatementList.add(stmt); continue;}
            if (!(((AssignStmt) stmt).getLHS() instanceof NameExpr)) {newPrefixStatementList.add(stmt); continue;}
            if (!(((AssignStmt) stmt).getRHS() instanceof NameExpr)) {newPrefixStatementList.add(stmt); continue;}
            String lhsName = ((NameExpr) ((AssignStmt) stmt).getLHS()).getName().getID();
            String rhsName = ((NameExpr) ((AssignStmt) stmt).getRHS()).getName().getID();
            if (!argument.alterNamespace.getDefinedNameSet().contains(lhsName)) {newPrefixStatementList.add(stmt); continue;}
            if (!argument.alterNamespace.getDefinedNameSet().contains(rhsName)) {newPrefixStatementList.add(stmt); continue;}
            substituteMap.put(lhsName, rhsName);
        }

        java.util.function.Function<ASTNode, ASTNode> transform = new Function<ASTNode, ASTNode>() {
            @Override
            public ASTNode apply(ASTNode astNode) {
                if (astNode instanceof ast.Name) {
                    String currentName = ((Name) astNode).getID();
                    if (substituteMap.containsKey(currentName)) {
                        ((Name) astNode).setID(substituteMap.get(currentName));
                        return astNode;
                    } else {
                        return astNode;
                    }
                }
                for (int iter = 0; iter < astNode.getNumChild(); iter++) {
                    astNode.setChild(apply(astNode.getChild(iter)), iter);
                }
                return astNode;
            }
        };

        List<Stmt> retList = new LinkedList<>();
        for (Stmt stmt : newPrefixStatementList) {
            retList.add((Stmt)transform.apply(stmt));
        }

        Expr retExpr = (Expr) transform.apply(candidate.getValue0());

        return new Pair<>(retExpr, retList);
    }
}
