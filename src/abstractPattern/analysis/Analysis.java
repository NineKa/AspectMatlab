package abstractPattern.analysis;

import ast.AndExpr;
import ast.Expr;
import ast.NotExpr;
import ast.OrExpr;

import java.util.HashMap;
import java.util.Map;

public class Analysis {
    private Map<Expr, PatternType> resultMap = new HashMap<>();

    public PatternType getResult(Expr pattern) {
        return this.resultMap.get(pattern);
    }

    @SuppressWarnings("ann-dep")
    public Analysis(Expr pattern) {
        this.analysis(pattern);
    }

    @Deprecated
    public void analysis(Expr pattern) {
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
                resultMap.put(pattern, PatternType.andMerge(leftClass, rightClass));
            }
            if (pattern instanceof OrExpr) {
                this.analysis(((OrExpr)pattern).getLHS());
                this.analysis(((OrExpr)pattern).getRHS());
                PatternType leftClass = resultMap.get(((OrExpr)pattern).getLHS());
                PatternType rightClass = resultMap.get(((OrExpr)pattern).getRHS());
                resultMap.put(pattern, PatternType.orMerge(leftClass, rightClass));
            }
            if (pattern instanceof NotExpr) {
                this.analysis(((NotExpr)pattern).getOperand());
                PatternType operandClass = resultMap.get(((NotExpr)pattern).getOperand());
                resultMap.put(pattern, PatternType.notMerge(operandClass));
            }
        }
    }
}
