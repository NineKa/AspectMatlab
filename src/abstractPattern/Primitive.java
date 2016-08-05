package abstractPattern;

import Matlab.Utils.IReport;
import abstractPattern.analysis.PatternClassifier;
import abstractPattern.modifier.ModifierAnd;
import abstractPattern.type.WeaveType;
import ast.*;
import org.javatuples.Pair;
import transformer.util.IsPossibleJointPointResult;
import transformer.util.RuntimeInfo;

import java.util.*;
import java.util.List;
import java.util.function.Function;

public abstract class Primitive extends Pattern{
    private List<Modifier> modifiers = new LinkedList<>();

    public void addModifier(Modifier modifier) {
        if (modifier instanceof ModifierAnd) {
            this.addModifier(((ModifierAnd)modifier).getLHS());
            this.addModifier(((ModifierAnd)modifier).getRHS());
        } else {
            this.modifiers.add(modifier);
        }
    }

    public boolean isModified() {
        return !this.modifiers.isEmpty();
    }

    public List<Modifier> getModifiers(){
        return this.modifiers;
    }

    public Collection<Modifier> getBadicModifierSet() {
        Collection<Modifier> collection = new HashSet<>();
        for (Modifier modifier: this.getModifiers()) {
            collection.addAll(modifier.getAllModfiierSet());
        }
        return collection;
    }

    public void clearModifiers() {
        this.modifiers.clear();
    }

    public abstract boolean isProperlyModified();

    public abstract IReport getModifierValidationReport(String pFilepath);

    public abstract Map<WeaveType, Boolean> getWeaveInfo();

    public void reformatModifier() {
        /* peephole reformat on tree (remove doubleNeg, logical reduction) */
        Function<Expr, Pair<Boolean, Expr>> reduceDoubleNeg = new Function<Expr, Pair<Boolean, Expr>>() {
            /* !!A <=> A*/
            @Override
            public Pair<Boolean, Expr> apply(Expr expr) {
                if (expr instanceof NotExpr && ((NotExpr) expr).getOperand() instanceof NotExpr) {
                    Expr modifiedExpr = ((NotExpr) ((NotExpr) expr).getOperand()).getOperand();
                    return new Pair<>(true, modifiedExpr);
                }
                /* recursive */
                if (expr instanceof PatternDimension) return new Pair<>(false, expr);
                if (expr instanceof PatternIsType) return new Pair<>(false, expr);
                if (expr instanceof PatternWithin) return new Pair<>(false, expr);
                if (expr instanceof AndExpr) {
                    Pair<Boolean, Expr> lhsResult = this.apply(((AndExpr) expr).getLHS());
                    Pair<Boolean, Expr> rhsResult = this.apply(((AndExpr) expr).getRHS());
                    if (lhsResult.getValue0()) ((AndExpr) expr).setLHS(lhsResult.getValue1());
                    if (rhsResult.getValue0()) ((AndExpr) expr).setRHS(rhsResult.getValue1());
                    return new Pair<>(lhsResult.getValue0() || rhsResult.getValue0(), expr);
                }
                if (expr instanceof OrExpr) {
                    Pair<Boolean, Expr> lhsResult = this.apply(((OrExpr) expr).getLHS());
                    Pair<Boolean, Expr> rhsResult = this.apply(((OrExpr) expr).getRHS());
                    if (lhsResult.getValue0()) ((OrExpr) expr).setLHS(lhsResult.getValue1());
                    if (rhsResult.getValue0()) ((OrExpr) expr).setRHS(rhsResult.getValue1());
                    return new Pair<>(lhsResult.getValue0() || rhsResult.getValue0(), expr);
                }
                if (expr instanceof NotExpr) {
                    Pair<Boolean, Expr> result = this.apply(((NotExpr) expr).getOperand());
                    if (result.getValue0()) ((NotExpr) expr).setOperand(result.getValue1());
                    return new Pair<>(result.getValue0(), expr);
                }
                /* control flow should not reach here */
                throw new AssertionError();
            }
        };
        Function<Expr, Pair<Boolean, Expr>> reduceAndNeg = new Function<Expr, Pair<Boolean, Expr>>() {
            /* !(A && B) <=> !A || !B */
            @Override
            public Pair<Boolean, Expr> apply(Expr expr) {
                if (expr instanceof NotExpr && ((NotExpr) expr).getOperand() instanceof AndExpr) {
                    Expr newLHS = new NotExpr(((AndExpr) ((NotExpr) expr).getOperand()).getLHS());
                    Expr newRHS = new NotExpr(((AndExpr) ((NotExpr) expr).getOperand()).getRHS());
                    OrExpr retExpr = new OrExpr(newLHS, newRHS);
                    return new Pair<>(true, retExpr);
                }
                /* recursive */
                if (expr instanceof PatternDimension) return new Pair<>(false, expr);
                if (expr instanceof PatternIsType) return new Pair<>(false, expr);
                if (expr instanceof PatternWithin) return new Pair<>(false, expr);
                if (expr instanceof AndExpr) {
                    Pair<Boolean, Expr> lhsResult = this.apply(((AndExpr) expr).getLHS());
                    Pair<Boolean, Expr> rhsResult = this.apply(((AndExpr) expr).getRHS());
                    if (lhsResult.getValue0()) ((AndExpr) expr).setLHS(lhsResult.getValue1());
                    if (rhsResult.getValue0()) ((AndExpr) expr).setRHS(rhsResult.getValue1());
                    return new Pair<>(lhsResult.getValue0() || rhsResult.getValue0(), expr);
                }
                if (expr instanceof OrExpr) {
                    Pair<Boolean, Expr> lhsResult = this.apply(((OrExpr) expr).getLHS());
                    Pair<Boolean, Expr> rhsResult = this.apply(((OrExpr) expr).getRHS());
                    if (lhsResult.getValue0()) ((OrExpr) expr).setLHS(lhsResult.getValue1());
                    if (rhsResult.getValue0()) ((OrExpr) expr).setRHS(rhsResult.getValue1());
                    return new Pair<>(lhsResult.getValue0() || rhsResult.getValue0(), expr);
                }
                if (expr instanceof NotExpr) {
                    Pair<Boolean, Expr> result = this.apply(((NotExpr) expr).getOperand());
                    if (result.getValue0()) ((NotExpr) expr).setOperand(result.getValue1());
                    return new Pair<>(result.getValue0(), expr);
                }
                /* control flow should not reach here */
                throw new AssertionError();
            }
        };
        Function<Expr, Pair<Boolean, Expr>> reduceOrNeg = new Function<Expr, Pair<Boolean, Expr>>() {
            /* !(A || B) <=> !A && !B */
            @Override
            public Pair<Boolean, Expr> apply(Expr expr) {
                if (expr instanceof NotExpr && ((NotExpr) expr).getOperand() instanceof OrExpr) {
                    Expr newLHS = new NotExpr(((OrExpr) ((NotExpr) expr).getOperand()).getLHS());
                    Expr newRHS = new NotExpr(((OrExpr) ((NotExpr) expr).getOperand()).getRHS());
                    AndExpr retExpr = new AndExpr(newLHS, newRHS);
                    return new Pair<>(true, retExpr);
                }
                /* recursive */
                if (expr instanceof PatternDimension) return new Pair<>(false, expr);
                if (expr instanceof PatternIsType) return new Pair<>(false, expr);
                if (expr instanceof PatternWithin) return new Pair<>(false, expr);
                if (expr instanceof AndExpr) {
                    Pair<Boolean, Expr> lhsResult = this.apply(((AndExpr) expr).getLHS());
                    Pair<Boolean, Expr> rhsResult = this.apply(((AndExpr) expr).getRHS());
                    if (lhsResult.getValue0()) ((AndExpr) expr).setLHS(lhsResult.getValue1());
                    if (rhsResult.getValue0()) ((AndExpr) expr).setRHS(rhsResult.getValue1());
                    return new Pair<>(lhsResult.getValue0() || rhsResult.getValue0(), expr);
                }
                if (expr instanceof OrExpr) {
                    Pair<Boolean, Expr> lhsResult = this.apply(((OrExpr) expr).getLHS());
                    Pair<Boolean, Expr> rhsResult = this.apply(((OrExpr) expr).getRHS());
                    if (lhsResult.getValue0()) ((OrExpr) expr).setLHS(lhsResult.getValue1());
                    if (rhsResult.getValue0()) ((OrExpr) expr).setRHS(rhsResult.getValue1());
                    return new Pair<>(lhsResult.getValue0() || rhsResult.getValue0(), expr);
                }
                if (expr instanceof NotExpr) {
                    Pair<Boolean, Expr> result = this.apply(((NotExpr) expr).getOperand());
                    if (result.getValue0()) ((NotExpr) expr).setOperand(result.getValue1());
                    return new Pair<>(result.getValue0(), expr);
                }
                /* control flow should not reach here */
                throw new AssertionError();
            }
        };

        List<Modifier> modifiedModifiers = new LinkedList<>();
        for (Modifier modifier : this.getModifiers()) {
            assert modifier.getASTExpr() instanceof Expr;
            Expr orgExpr = (Expr)modifier.getASTExpr();
            boolean flagChanged = true;
            while (flagChanged) {
                flagChanged = false;
                /* apply reduce double negation function*/
                Pair<Boolean, Expr> reduceDoubleNegResult = reduceDoubleNeg.apply(orgExpr);
                orgExpr = reduceDoubleNegResult.getValue1();
                if (reduceDoubleNegResult.getValue0() /* modified mark */) {
                    flagChanged = true;
                    continue;
                }
                /* apply reduce not-and function*/
                Pair<Boolean, Expr> reduceAndNegResult = reduceAndNeg.apply(orgExpr);
                orgExpr = reduceAndNegResult.getValue1();
                if (reduceAndNegResult.getValue0() /* modified mark */) {
                    flagChanged = true;
                    continue;
                }
                /* apply reduce not-or function */
                Pair<Boolean, Expr> reduceOrNegResult = reduceOrNeg.apply(orgExpr);
                orgExpr = reduceOrNegResult.getValue1();
                if (reduceOrNegResult.getValue0() /* modified mark */) {
                    flagChanged = true;
                    continue;
                }
                /* keep iterating until no further change in AST */
            }
            modifiedModifiers.add(PatternClassifier.buildModifier(orgExpr));
        }

        this.clearModifiers();
        for (Modifier modifier : modifiedModifiers) {
            this.addModifier(modifier);
        }
    }

    /* --- transform --- */
    public IsPossibleJointPointResult isPossibleJointPoint(ASTNode astNode, RuntimeInfo runtimeInfo) {
        /* TODO: abstract this method */
        return new IsPossibleJointPointResult();
    }
    /* ----------------- */
}
