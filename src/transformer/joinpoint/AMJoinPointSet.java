package transformer.joinpoint;

import abstractPattern.analysis.PatternType;
import ast.Expr;
import ast.NameExpr;
import ast.Stmt;

public final class AMJoinPointSet extends AMJoinPoint {
    private Expr indicesExpr = null;
    private NameExpr oldVarExpr = null;
    private NameExpr newVarExpr = null;

    public AMJoinPointSet(Stmt inlineStatement, int startLine, int startColumn, String filepath) {
        super(inlineStatement, startLine, startColumn, filepath);
    }

    @Override
    public PatternType getMatchedPatternType() {
        return PatternType.Set;
    }

    public Expr getIndicesExpr() {
        return indicesExpr;
    }

    public void setIndicesExpr(Expr indicesExpr) {
        this.indicesExpr = indicesExpr;
    }

    public NameExpr getOldVarExpr() {
        return oldVarExpr;
    }

    public void setOldVarExpr(NameExpr oldVarExpr) {
        this.oldVarExpr = oldVarExpr;
    }

    public String getOldVarName() {
        return this.oldVarExpr.getName().getID();
    }

    public NameExpr getNewVarExpr() {
        return newVarExpr;
    }

    public void setNewVarExpr(NameExpr newVarExpr) {
        this.newVarExpr = newVarExpr;
    }

    public String getNewVarName() {
        return this.newVarExpr.getName().getID();
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
