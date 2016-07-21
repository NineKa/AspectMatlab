package abstractPattern;

import Matlab.Utils.Report;
import abstractPattern.analysis.Analysis;
import abstractPattern.analysis.Backtrace;
import abstractPattern.analysis.PatternClassifier;
import abstractPattern.analysis.PatternType;
import abstractPattern.primitive.And;
import abstractPattern.primitive.Or;
import ast.*;

import java.util.Map;
import java.util.Stack;

public class AbstractBuilder {
    private Analysis analysis = null;
    private Report report = new Report();
    private Expr patternExpr = null;
    private Map<String, Expr> definedMap = null;
    private String filepath = null;

    private Stack<String> definedStack = new Stack<>();

    private Pattern pattern = null;

    public AbstractBuilder(String pFilepath, Expr expr, Map<String, Expr> definedMap) {
        try {
            this.definedMap = definedMap;
            this.filepath = pFilepath;

            this.patternExpr = getSubstitutedExpr(expr.treeCopy());

            this.analysis = new Analysis(pFilepath, this.patternExpr);

            if (this.analysis.getResult() == PatternType.Modifier) {
                this.pattern = PatternClassifier.buildModifier(this.patternExpr);
            }
            if (this.analysis.getResult() == PatternType.Primitive) {
                this.pattern = this.buildPrimitive(this.patternExpr);
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

    private Expr getSubstitutedExpr(Expr expr) throws Backtrace{
        if (PatternClassifier.isBasicPattern(expr)) return expr;
        if (expr instanceof AndExpr) {
            ((AndExpr) expr).setLHS(getSubstitutedExpr(((AndExpr) expr).getLHS()));
            ((AndExpr) expr).setRHS(getSubstitutedExpr(((AndExpr) expr).getRHS()));
            return expr;
        }
        if (expr instanceof OrExpr) {
            ((OrExpr) expr).setLHS(getSubstitutedExpr(((OrExpr) expr).getLHS()));
            ((OrExpr) expr).setRHS(getSubstitutedExpr(((OrExpr) expr).getRHS()));
            return expr;
        }
        if (expr instanceof NotExpr) {
            ((NotExpr) expr).setOperand(getSubstitutedExpr(((NotExpr) expr).getOperand()));
            return expr;
        }
        if (expr instanceof PatternName) {
            String id = ((PatternName) expr).getName().getID();
            if (!this.definedMap.containsKey(id)) {
                throw new Backtrace(
                        this.filepath,
                        expr.getStartLine(),
                        expr.getStartColumn(),
                        String.format("unbounded pattern identifier : %s", id)
                );
            }
            if (this.definedStack.contains(id)) {
                throw new Backtrace(
                        this.filepath,
                        expr.getStartLine(),
                        expr.getStartColumn(),
                        String.format("cannot resolve the dependency of pattern %s", id)
                );
            }
            this.definedStack.push(id);
            Expr retExpr = getSubstitutedExpr(this.definedMap.get(id)).treeCopy();
            this.definedStack.pop();
            return retExpr;
        }
        /* control flow should not reach here */
        throw new RuntimeException();
    }
}
