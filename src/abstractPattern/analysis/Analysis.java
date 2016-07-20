package abstractPattern.analysis;

import ast.AndExpr;
import ast.Expr;
import ast.NotExpr;
import ast.OrExpr;

import java.util.HashMap;
import java.util.Map;

public class Analysis {
    private String filepath = "Not yet implement";
    private Map<Expr, PatternType> resultMap = new HashMap<>();
    private Expr patternRoot = null;

    public PatternType getResult(Expr pattern) {
        return this.resultMap.get(pattern);
    }

    public PatternType getResult() {
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
                resultMap.put(pattern, PatternType.Modifier);
            }
            if (PatternClassifier.isBasicPatternPrimitive(pattern)) {
                resultMap.put(pattern, PatternType.Primitive);
            }
        } else {
            if (pattern instanceof AndExpr) {
                this.analysis(((AndExpr)pattern).getLHS());
                this.analysis(((AndExpr)pattern).getRHS());
                PatternType leftClass = resultMap.get(((AndExpr)pattern).getLHS());
                PatternType rightClass = resultMap.get(((AndExpr)pattern).getRHS());
                /* --- throw back trace --- */
                PatternType thisAnalysisType = PatternType.andMerge(leftClass, rightClass);
                if (thisAnalysisType == PatternType.Invalid) {
                    /* control flow should not reach here */
                    throw new RuntimeException();
                }
                resultMap.put(pattern, thisAnalysisType);
            }
            if (pattern instanceof OrExpr) {
                this.analysis(((OrExpr)pattern).getLHS());
                this.analysis(((OrExpr)pattern).getRHS());
                PatternType leftClass = resultMap.get(((OrExpr)pattern).getLHS());
                PatternType rightClass = resultMap.get(((OrExpr)pattern).getRHS());
                /* --- throw back trace --- */
                PatternType thisAnalysisType = PatternType.orMerge(leftClass, rightClass);
                if (thisAnalysisType == PatternType.Invalid) {
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
                PatternType operandClass = resultMap.get(((NotExpr)pattern).getOperand());
                /* --- throw back trace --- */
                PatternType thisAnalysisType = PatternType.notMerge(operandClass);
                if (thisAnalysisType == PatternType.Invalid) {
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
