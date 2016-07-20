package abstractPattern;

import Matlab.Utils.Report;
import abstractPattern.analysis.Analysis;
import abstractPattern.analysis.Backtrace;
import abstractPattern.analysis.PatternClassifier;
import abstractPattern.analysis.PatternType;
import abstractPattern.primitive.And;
import abstractPattern.primitive.Or;
import ast.AndExpr;
import ast.Expr;
import ast.OrExpr;

public class Builder {
    private Analysis analysis = null;
    private Report report = new Report();
    private Expr patternExpr = null;

    private Pattern pattern = null;

    public Builder(String pFilepath, Expr expr) {
        try {
            this.patternExpr = expr;
            this.analysis = new Analysis(pFilepath, expr);
            if (this.analysis.getResult() == PatternType.Modifier) {
                this.pattern = PatternClassifier.buildModifier(expr);
            }
            if (this.analysis.getResult() == PatternType.Primitive) {
                this.pattern = this.buildPrimitive(expr);
            }
        } catch (Backtrace backtrace) {
            report.Add(backtrace.getBacktraceMsg());
        }
    }

    private Primitive buildPrimitive(Expr expr) {
        if (PatternClassifier.isBasicPattern(expr) && PatternClassifier.isBasicPatternPrimitive(expr)) {
            return PatternClassifier.buildBasicPrimitive(expr);
        }
        if (expr instanceof AndExpr) {
            Expr lhs = ((AndExpr)expr).getLHS();
            Expr rhs = ((AndExpr)expr).getRHS();
            if (this.analysis.getResult(lhs) == PatternType.Modifier) {
                assert this.analysis.getResult(rhs) != PatternType.Modifier;
                Primitive retNode = buildPrimitive(rhs);
                retNode.addModifier(PatternClassifier.buildModifier(lhs));
                return retNode;
            }
            if (this.analysis.getResult(rhs) == PatternType.Modifier) {
                assert this.analysis.getResult(lhs) != PatternType.Modifier;
                Primitive retNode = buildPrimitive(lhs);
                retNode.addModifier(PatternClassifier.buildModifier(rhs));
                return retNode;
            }
            And retNode = new And();
            retNode.setAndExpr((AndExpr)expr);
            retNode.setLHS(buildPrimitive(lhs));
            retNode.setRHS(buildPrimitive(rhs));
            return retNode;
        }
        if (expr instanceof OrExpr) {
            Expr lhs = ((OrExpr)expr).getLHS();
            Expr rhs = ((OrExpr)expr).getRHS();

            assert this.analysis.getResult(lhs) != PatternType.Modifier;
            assert this.analysis.getResult(rhs) != PatternType.Modifier;

            Or retNode = new Or();
            retNode.setOrExpr((OrExpr)expr);
            retNode.setLHS(buildPrimitive(lhs));
            retNode.setRHS(buildPrimitive(rhs));
            return retNode;
        }
        /* cotnrol flow should not reach here */
        throw new RuntimeException();
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Report getReport() {
        return report;
    }
}
