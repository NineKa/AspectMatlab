package abstractPattern.analysis;

import ast.AndExpr;
import ast.Expr;
import ast.NotExpr;
import ast.OrExpr;

import java.util.HashMap;
import java.util.Map;

public class Analysis {
    private String filepath = "Not yet implement";
    private Map<Expr, PatternAnalysisType> resultMap = new HashMap<>();
    private Expr patternRoot = null;

    public PatternAnalysisType getResult(Expr pattern) {
        return this.resultMap.get(pattern);
    }

    public PatternAnalysisType getResult() {
        return this.resultMap.get(patternRoot);
    }

    @SuppressWarnings("ann-dep")
    public Analysis(String pFilepath, Expr pattern) throws Backtrace{
        this.filepath = pFilepath;
        this.patternRoot = pattern;
        this.analysis(pattern);
    }

    @Deprecated
    public void analysis(Expr pattern) throws Backtrace{
        if (PatternClassifier.isBasicPattern(pattern)) {
            if (PatternClassifier.isBasicPatternModifier(pattern)) {
                resultMap.put(pattern, PatternAnalysisType.Modifier);
            }
            if (PatternClassifier.isBasicPatternPrimitive(pattern)) {
                resultMap.put(pattern, PatternAnalysisType.Primitive);
            }
        } else {
            if (pattern instanceof AndExpr) {
                this.analysis(((AndExpr)pattern).getLHS());
                this.analysis(((AndExpr)pattern).getRHS());
                PatternAnalysisType leftClass = resultMap.get(((AndExpr)pattern).getLHS());
                PatternAnalysisType rightClass = resultMap.get(((AndExpr)pattern).getRHS());
                /* --- throw back trace --- */
                PatternAnalysisType thisAnalysisType = PatternAnalysisType.andMerge(leftClass, rightClass);
                if (thisAnalysisType == PatternAnalysisType.Invalid) {
                    /* control flow should not reach here */
                    throw new RuntimeException();
                }
                resultMap.put(pattern, thisAnalysisType);
            }
            if (pattern instanceof OrExpr) {
                this.analysis(((OrExpr)pattern).getLHS());
                this.analysis(((OrExpr)pattern).getRHS());
                PatternAnalysisType leftClass = resultMap.get(((OrExpr)pattern).getLHS());
                PatternAnalysisType rightClass = resultMap.get(((OrExpr)pattern).getRHS());
                /* --- throw back trace --- */
                PatternAnalysisType thisAnalysisType = PatternAnalysisType.orMerge(leftClass, rightClass);
                if (thisAnalysisType == PatternAnalysisType.Invalid) {
                    /* attempt to concatenating a primitive pattern and a modifier pattern */
                    throw new Backtrace(
                            this.filepath,
                            pattern.getStartLine(),
                            pattern.getStartColumn(),
                            "Invalid pattern (attempt to concatenating a primitive pattern to a modifier pattern using OR operation, this action may result to confusing result)"
                    );
                }
                resultMap.put(pattern, thisAnalysisType);
            }
            if (pattern instanceof NotExpr) {
                this.analysis(((NotExpr)pattern).getOperand());
                PatternAnalysisType operandClass = resultMap.get(((NotExpr)pattern).getOperand());
                /* --- throw back trace --- */
                PatternAnalysisType thisAnalysisType = PatternAnalysisType.notMerge(operandClass);
                if (thisAnalysisType == PatternAnalysisType.Invalid) {
                    throw new Backtrace(
                            this.filepath,
                            pattern.getStartLine(),
                            pattern.getStartColumn(),
                            "Invalid pattern (attempt to apply NOT operation on a primitive pattern, this action may result to confusing result)"
                    );
                }
                resultMap.put(pattern, thisAnalysisType);
            }
        }
    }
}
