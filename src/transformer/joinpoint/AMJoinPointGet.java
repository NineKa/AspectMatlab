package transformer.joinpoint;

import abstractPattern.analysis.PatternType;
import ast.Expr;
import ast.NameExpr;
import ast.Stmt;

public final class AMJoinPointGet extends AMJoinPoint {
    private Expr indicesExpr = null;
    private NameExpr targetExpr = null;

    public AMJoinPointGet(Stmt inlineStatement, int startLine, int startColumn, String filepath) {
        super(inlineStatement, startLine, startColumn, filepath);
    }

    @Override
    public PatternType getMatchedPatternType() {
        return PatternType.Get;
    }

    public Expr getIndicesExpr() {
        return indicesExpr;
    }

    public void setIndicesExpr(Expr indicesExpr) {
        this.indicesExpr = indicesExpr;
    }

    public NameExpr getTargetVariable() {
        return this.targetExpr;
    }

    public void setTargetExpr(NameExpr targetExpr) {
        this.targetExpr = targetExpr;
    }

    public String getTargetName() {
        return this.targetExpr.getName().getID();
    }

    @Override
    public String toString() {
        return String.format(
                "[%10s] [%3d:%3d] %s with indices: %s, actions: %s",
                getMatchedPatternType(),
                getStartLine(),
                getStartColumn(),
                getInlineStatement().getPrettyPrinted().trim(),
                getIndicesExpr().getPrettyPrinted().trim(),
                getMatchedActions().toString()
        );
    }
}
